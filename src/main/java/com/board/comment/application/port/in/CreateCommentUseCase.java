package com.board.comment.application.port.in;

public interface CreateCommentUseCase {

    CommentResult createComment(CreateCommentCommand command);

    record CreateCommentCommand(Long postId, Long memberId, String content, Long parentId) {
    }
}
