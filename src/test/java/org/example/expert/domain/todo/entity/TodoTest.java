package org.example.expert.domain.todo.entity;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TodoTest {

    @Test
    @DisplayName("일정 업데이트 성공")
    public void update_일정을_정상적으로_업데이트한다() {
        // given
        User user = new User("test@test.com", "Password1234!", UserRole.USER);
        Todo todo = new Todo("title", "contents", "맑음", user);

        // when
        todo.update("newTitle", "newContents");

        // then
        assertEquals("newTitle", todo.getTitle());
        assertEquals("newContents", todo.getContents());
    }
}