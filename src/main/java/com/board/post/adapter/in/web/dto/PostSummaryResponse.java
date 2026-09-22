package com.board.post.adapter.in.web.dto;

import com.board.post.application.port.in.GetPostListUseCase.PostSummaryResult;
import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long commentCount
) {

    public static PostSummaryResponse from(PostSummaryResult result) {
        return new PostSummaryResponse(
                result.id(), result.title(), result.authorNickname(), result.createdAt(), result.updatedAt(),
                result.commentCount()
        );
    }
}
