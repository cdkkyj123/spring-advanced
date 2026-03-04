package org.example.expert.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.aspect.AdminApiLog;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @AdminApiLog
    @DeleteMapping("/admin/comments/{commentId}")
    public void deleteComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long commentId
    ) {
        commentAdminService.deleteComment(authUser, commentId);
    }
}
