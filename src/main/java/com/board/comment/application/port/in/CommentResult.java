package com.board.comment.application.port.in;

import java.time.LocalDateTime;

public record CommentResult(
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
