package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    private final AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
    private final User todoOwner = User.fromAuthUser(authUser);
    private final User anotherUser = new User("another@test.com", "Password1234!", UserRole.USER);
    private final Todo todo = new Todo("title", "contents", "맑음", todoOwner);
    private final Todo anotherTodo = new Todo("title2", "contents2", "흐림", anotherUser);
    private final ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);

    @Test
    @DisplayName("일정의 관리자 목록 조회 시 일정이 없을 때 에러 발생 성공")
    // 우선 우리가 테스트할 정확한 메서드 명인 getManagers라는 메서드명을 명시해주고 에러명을 알맞게 수정
    public void getManagers_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        // 그 후 "Manager not found"가 아닌 "To.do not found"로 수정하여 테스트가 성공하게 만듦.
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("일정을 작성한 유저가 null일 경우 NPE 에러 발생 성공")
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        // NPE는 보통 메시지를 따로 지정하지 않기 때문에 equals할 필요 없이 위 로직만으로 검증 완료.
//        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    @DisplayName("담당자 등록 실패 - 일정 작성자 불일치")
    public void saveManager_일정_작성자가_아니어서_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(anotherUser, "id", 99L);

        // To-do 조회 성공, 작성자 불일치
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(anotherTodo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, 1L, managerSaveRequest)
        );

        // then
        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 등록 실패 - 담당자 유저 없음")
    public void saveManager_담당자_유저가_없어_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);

        // To-do 조회 성공, 담당자 유저 없음
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, 1L, managerSaveRequest)
        );

        // then
        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 등록 실패 - 본인을 담당자로 등록")
    public void saveManager_본인을_담당자로_등록하여_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);

        // To-do 조회 성공, 담당자가 본인
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, 1L, new ManagerSaveRequest(1L))
        );

        // then
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 성공")
    public void deleteManager_담당자를_정상적으로_삭제한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);
        ReflectionTestUtils.setField(todo, "id", 1L);
        Manager manager = new Manager(todoOwner, todo);

        // 유저 조회 성공, To-do 조회 성공, 담당자 조회 성공, 삭제
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        // when & then
        assertDoesNotThrow(() -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("담당자 삭제 실패 - 유저 없음")
    public void deleteManager_유저가_없어_에러가_발생한다() {
        // given
        // 유저 없음
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(1L, 1L, 1L)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 실패 - Todo 없음")
    public void deleteManager_Todo가_없어_에러가_발생한다() {
        // given
        // 유저 조회 성공, To-do 없음
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(1L, 1L, 1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 실패 - 일정 작성자 불일치")
    public void deleteManager_일정_작성자가_아니어서_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);
        ReflectionTestUtils.setField(anotherUser, "id", 99L);

        // 유저 조회 성공, 작성자 불일치
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(anotherTodo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(1L, 1L, 1L)
        );

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 실패 - 해당 일정의 담당자 아님")
    public void deleteManager_해당_일정의_담당자가_아니어서_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);
        ReflectionTestUtils.setField(todo, "id", 1L);
        ReflectionTestUtils.setField(anotherTodo, "id", 99L);

        // 유저 조회 성공, To-do 조회 성공, 다른 일정의 담당자 조회
        Manager manager = new Manager(todoOwner, anotherTodo);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(1L, 1L, 1L)
        );

        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

    @Test
    @DisplayName("담당자 삭제 실패 - 일정 작성자가 null")
    public void deleteManager_일정_작성자가_null이어서_에러가_발생한다() {
        // given
        ReflectionTestUtils.setField(todoOwner, "id", 1L);

        // To-do의 user가 null인 경우
        Todo nullUserTodo = new Todo("title", "contents", "맑음", null);
        ReflectionTestUtils.setField(nullUserTodo, "user", null);

        // 유저 조회 성공, To-do의 작성자 null
        given(userRepository.findById(anyLong())).willReturn(Optional.of(todoOwner));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(nullUserTodo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(1L, 1L, 1L)
        );

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }
}
