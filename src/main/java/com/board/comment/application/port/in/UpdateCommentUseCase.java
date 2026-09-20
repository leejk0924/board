package com.board.comment.application.port.in;

public interface UpdateCommentUseCase {

    CommentResult updateComment(UpdateCommentCommand command);

    record UpdateCommentCommand(Long commentId, Long memberId, String content) {
    }
}
