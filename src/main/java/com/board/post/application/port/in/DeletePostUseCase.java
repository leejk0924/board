package com.board.post.application.port.in;

public interface DeletePostUseCase {

    void deletePost(DeletePostCommand command);

    record DeletePostCommand(Long postId, Long memberId) {
    }
}
