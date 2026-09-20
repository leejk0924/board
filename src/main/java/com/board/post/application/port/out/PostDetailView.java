package com.board.post.application.port.out;

import java.time.LocalDateTime;

public record PostDetailView(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
