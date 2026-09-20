package com.board.post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long commentCount
) {
}
