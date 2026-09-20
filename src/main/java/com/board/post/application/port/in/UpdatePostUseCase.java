package com.board.post.application.port.in;

public interface UpdatePostUseCase {

    PostResult updatePost(UpdatePostCommand command);

    record UpdatePostCommand(Long postId, Long memberId, String title, String content) {
    }
}
