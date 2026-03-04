package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentRepository commentRepository;

    @Transactional
    public void deleteComment(AuthUser authUser, long commentId) {
        if (!authUser.getUserRole().equals(UserRole.ADMIN)) {
            throw new IllegalArgumentException("관리자만 댓글을 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(commentId);
    }
}
