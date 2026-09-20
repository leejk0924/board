package com.board.post.application.port.out;

import com.board.post.domain.Post;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);
}
