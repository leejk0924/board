package com.board.post.application.port.out;

import java.time.LocalDateTime;

public record PostSummaryView(
        Long id,
        String title,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long commentCount
) {
}
