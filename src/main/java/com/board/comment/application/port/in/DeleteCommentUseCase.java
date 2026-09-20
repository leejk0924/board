package com.board.comment.application.port.in;

public interface DeleteCommentUseCase {

    void deleteComment(DeleteCommentCommand command);

    record DeleteCommentCommand(Long commentId, Long memberId) {
    }
}
