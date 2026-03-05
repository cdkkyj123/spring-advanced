package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;
    @InjectMocks
    private TodoController todoController;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    private final User user = User.fromAuthUser(authUser);
    private final TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");

    @Test
    @DisplayName("일정 저장 성공")
    public void saveTodo_일정을_정상적으로_저장한다() {
        // given
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(
                1L, "title", "contents", "맑음",
                new UserResponse(user.getId(), user.getEmail())
        );

        // 일정 저장 성공
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class)))
                .willReturn(todoSaveResponse);

        // when
        ResponseEntity<TodoSaveResponse> result = todoController.saveTodo(authUser, todoSaveRequest);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(todoSaveResponse.getId(), result.getBody().getId());
        assertEquals(todoSaveResponse.getTitle(), result.getBody().getTitle());
    }

    @Test
    @DisplayName("일정 저장 실패 - 날씨 조회 실패")
    public void saveTodo_날씨_조회에_실패하여_에러가_발생한다() {
        // given
        // 날씨 조회 실패
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class)))
                .willThrow(new ServerException("날씨 데이터를 가져오는데 실패했습니다."));

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                todoController.saveTodo(authUser, todoSaveRequest)
        );

        // then
        assertEquals("날씨 데이터를 가져오는데 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("일정 목록 조회 성공")
    public void getTodos_일정_목록을_정상적으로_조회한다() {
        // given
        TodoResponse todoResponse = new TodoResponse(
                1L, "title", "contents", "맑음",
                new UserResponse(user.getId(), user.getEmail()),
                null, null
        );
        Page<TodoResponse> todoPage = new PageImpl<>(List.of(todoResponse));

        // 일정 목록 조회 성공
        given(todoService.getTodos(anyInt(), anyInt())).willReturn(todoPage);

        // when
        ResponseEntity<Page<TodoResponse>> result = todoController.getTodos(1, 10);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        assertEquals(todoResponse.getTitle(), result.getBody().getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("일정 단건 조회 성공")
    public void getTodo_일정을_정상적으로_조회한다() {
        // given
        TodoResponse todoResponse = new TodoResponse(
                1L, "title", "contents", "맑음",
                new UserResponse(user.getId(), user.getEmail()),
                null, null
        );

        // To-do 단건 조회 성공
        given(todoService.getTodo(anyLong())).willReturn(todoResponse);

        // when
        ResponseEntity<TodoResponse> result = todoController.getTodo(1L);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(todoResponse.getId(), result.getBody().getId());
        assertEquals(todoResponse.getTitle(), result.getBody().getTitle());
    }

    @Test
    @DisplayName("일정 단건 조회 실패 - To-do 없음")
    public void getTodo_To_do가_없어_에러가_발생한다() {
        // given
        // To-do 없음
        given(todoService.getTodo(anyLong()))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> todoController.getTodo(1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }
}