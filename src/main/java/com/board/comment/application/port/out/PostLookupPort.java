package com.board.comment.application.port.out;

public interface PostLookupPort {

    boolean existsById(Long postId);
}
