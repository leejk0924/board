package com.board.comment.application.port.out;

import java.util.List;

public interface CommentQueryRepository {

    List<CommentView> findAllByPostIdWithAuthor(Long postId);
}
