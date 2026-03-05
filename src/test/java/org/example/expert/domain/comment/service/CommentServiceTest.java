package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 등록 중 일정이 없을 때 에러 발생 성공")
    // 우선 메서드 명을 명확히 명시해준 뒤
    public void saveComment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        // To.do를 찾지 못 했을 때 발생하는 에러는 InvalidRequestException이기에
        // 알맞게 수정해줌.
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    private final User user1 = new User("test1@test.com", "Admin", UserRole.ADMIN);
    private final User user2 = new User("test2@test.com", "User", UserRole.USER);

    private final Todo todo1 = new Todo("title1", "contents1", "맑음", user1);
    private final Todo todo2 = new Todo("title2", "contents2", "맑음", user1);
    private final Todo todo3 = new Todo("title3", "contents3", "맑음", user2);

    private final Comment comment1 = new Comment("good1", user1, todo1);
    private final Comment comment2 = new Comment("good1", user2, todo1);
    private final Comment comment3 = new Comment("good1", user1, todo2);
    private final Comment comment4 = new Comment("good1", user1, todo3);
    private final Comment comment5 = new Comment("good1", user2, todo3);

    @Test
    @DisplayName("전체 조회 정상 동작 성공")
    public void getComments_성공() {

        // given
        long todoId = 1L;

        List<Comment> comments = new ArrayList<>();
        comments.add(comment1);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(comments);

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        verify(commentRepository, times(1)).findByTodoIdWithUser(todoId);
        assertEquals(comments.get(0).getId(), result.get(0).getId());
        assertEquals(user1.getId(), result.get(0).getUser().getId());
    }
}
