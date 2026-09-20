package com.board.post.application.port.in;

import java.time.LocalDateTime;

public record PostResult(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
