package com.board.comment.adapter.in.web.dto;

import com.board.comment.application.port.in.CommentResult;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorNickname,
        Long postId,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentResponse from(CommentResult result) {
        return new CommentResponse(
                result.id(), result.content(), result.authorId(), result.authorNickname(),
                result.postId(), result.parentId(), result.createdAt(), result.updatedAt()
        );
    }
}
