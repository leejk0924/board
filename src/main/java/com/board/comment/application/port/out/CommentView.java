package com.board.comment.application.port.out;

import java.time.LocalDateTime;

public record CommentView(
        Long id,
        String content,
        Long authorId,
        String authorNickname,
        Long postId,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
