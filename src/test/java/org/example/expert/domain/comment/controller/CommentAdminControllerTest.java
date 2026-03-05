package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentAdminControllerTest {

    @Mock
    private CommentAdminService commentAdminService;
    @InjectMocks
    private CommentAdminController commentAdminController;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.ADMIN);

    @Test
    @DisplayName("댓글 삭제 성공")
    public void deleteComment_댓글을_정상적으로_삭제한다() {
        // given
        // 댓글 삭제 성공
        doNothing().when(commentAdminService).deleteComment(any(AuthUser.class), anyLong());

        // when
        commentAdminController.deleteComment(authUser, 1L);

        // then
        verify(commentAdminService, times(1)).deleteComment(authUser, 1L);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 없음")
    public void deleteComment_댓글이_없어_에러가_발생한다() {
        // given
        // 댓글 없음
        doThrow(new InvalidRequestException("Comment not found"))
                .when(commentAdminService).deleteComment(any(AuthUser.class), anyLong());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                commentAdminController.deleteComment(authUser, 1L)
        );

        // then
        assertEquals("Comment not found", exception.getMessage());
    }
}