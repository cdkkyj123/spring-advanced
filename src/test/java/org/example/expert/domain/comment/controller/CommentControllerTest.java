package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    private final UserResponse userResponse = new UserResponse(1L, "test@test.com");

    @Test
    @DisplayName("댓글 저장 성공")
    public void saveComment_댓글을_정상적으로_등록한다() {
        // given
        CommentSaveRequest request = new CommentSaveRequest("내용");
        CommentSaveResponse response = new CommentSaveResponse(1L, "내용", userResponse);

        // 댓글 등록 성공
        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class)))
                .willReturn(response);

        // when
        ResponseEntity<CommentSaveResponse> result = commentController.saveComment(authUser, 1L, request);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response.getId(), result.getBody().getId());
        assertEquals(userResponse.getEmail(), result.getBody().getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 저장 실패 - Todo 없음")
    public void saveComment_Todo가_없어_에러가_발생한다() {
        // given
        CommentSaveRequest request = new CommentSaveRequest("내용");

        // To-do 없음
        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class)))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                commentController.saveComment(authUser, 1L, request)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    public void getComments_댓글_목록을_정상적으로_조회한다() {
        // given
        List<CommentResponse> responses = List.of(new CommentResponse(1L, "내용", userResponse));

        // 댓글 목록 조회 성공
        given(commentService.getComments(anyLong())).willReturn(responses);

        // when
        ResponseEntity<List<CommentResponse>> result = commentController.getComments(1L);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(userResponse.getEmail(), result.getBody().get(0).getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 목록 조회 실패 - Todo 없음")
    public void getComments_Todo가_없어_에러가_발생한다() {
        // given
        // To-do 없음
        given(commentService.getComments(anyLong()))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                commentController.getComments(1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }
}