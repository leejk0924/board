package com.board.post.adapter.in.web.dto;

import com.board.post.application.port.in.PostResult;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(PostResult result) {
        return new PostResponse(
                result.id(), result.title(), result.content(), result.authorId(), result.authorNickname(),
                result.createdAt(), result.updatedAt()
        );
    }
}
