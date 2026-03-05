package org.example.expert.domain.comment.entity;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class CommentTest {

    @Test
    @DisplayName("업데이트가 잘 되는지 확인 성공")
    public void update_success() {
        // given
        User user1 = new User("test1@test.com", "Admin", UserRole.ADMIN);
        Todo todo1 = new Todo("title1", "contents1", "맑음", user1);
        Comment comment1 = new Comment("good1", user1, todo1);

        // when
        comment1.update("goodgood");

        // then
        assertEquals("goodgood", comment1.getContents());
    }
}