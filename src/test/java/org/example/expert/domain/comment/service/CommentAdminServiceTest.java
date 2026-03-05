package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private CommentAdminService commentAdminService;

    private final User user1 = new User("test1@test.com", "Admin", UserRole.ADMIN);
    private final Todo todo1 = new Todo("title1", "contents1", "맑음", user1);
    private final Comment comment1 = new Comment("good1", user1, todo1);

    @Test
    @DisplayName("댓글 삭제에 성공")
    public void deleteComment_성공() {

        // given
        long commentId = 1L;

        AuthUser authUser = new AuthUser(1L, "tesfasf", UserRole.ADMIN);

        // when & then
        commentAdminService.deleteComment(authUser, commentId);
    }

    @Test
    @DisplayName("관리자가 아닐 시 예외처리 성공")
    public void deleteComment_예외발생_성공() {
        // given
        long commentId = 1L;

        AuthUser authUser = new AuthUser(1L, "tesfasf", UserRole.USER);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentAdminService.deleteComment(authUser, commentId);
        });

        // then
        assertEquals("관리자만 댓글을 삭제할 수 있습니다.", exception.getMessage());
    }
}