package com.board.post.application.port.out;

public interface CommentCleanupPort {

    void deleteAllByPostId(Long postId);
}
