package com.board.post.application.port.in;

public interface CreatePostUseCase {

    PostResult createPost(CreatePostCommand command);

    record CreatePostCommand(Long memberId, String title, String content) {
    }
}
