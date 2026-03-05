package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    private final User user = User.fromAuthUser(authUser);
    private final TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");

    @Test
    @DisplayName("일정 저장 성공")
    public void saveTodo_일정을_정상적으로_저장한다() {
        // given
        Todo savedTodo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(savedTodo, "id", 1L);

        // 날씨 조회 성공, To-do 저장
        given(weatherClient.getTodayWeather()).willReturn("맑음");
        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse result = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertNotNull(result);
        assertEquals(savedTodo.getId(), result.getId());
        assertEquals(savedTodo.getTitle(), result.getTitle());
        assertEquals(savedTodo.getContents(), result.getContents());
        assertEquals("맑음", result.getWeather());
    }

    @Test
    @DisplayName("일정 목록 조회 성공")
    public void getTodos_일정_목록을_정상적으로_조회한다() {
        // given
        Todo todo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        // 페이징 처리된 To-do 목록 조회
        Pageable pageable = PageRequest.of(0, 10);
        Page<Todo> todoPage = new PageImpl<>(List.of(todo), pageable, 1);
        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todoPage);

        // when
        Page<TodoResponse> result = todoService.getTodos(1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(todo.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(todo.getContents(), result.getContent().get(0).getContents());
    }

    @Test
    @DisplayName("일정 단건 조회 성공")
    public void getTodo_일정을_정상적으로_조회한다() {
        // given
        Todo todo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        // To-do 단건 조회 성공
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse result = todoService.getTodo(1L);

        // then
        assertNotNull(result);
        assertEquals(todo.getId(), result.getId());
        assertEquals(todo.getTitle(), result.getTitle());
        assertEquals(todo.getContents(), result.getContents());
    }

    @Test
    @DisplayName("일정 단건 조회 실패 - To-do 없음")
    public void getTodo_To_do가_없어_에러가_발생한다() {
        // given
        // To-do 없음
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                todoService.getTodo(1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }
}