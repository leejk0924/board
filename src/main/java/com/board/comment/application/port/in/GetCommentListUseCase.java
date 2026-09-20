package com.board.comment.application.port.in;

import java.util.List;

public interface GetCommentListUseCase {

    List<CommentResult> getComments(Long postId);
}
