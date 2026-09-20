package com.board.post.adapter.out.persistence;

import com.board.post.application.port.out.PostRepository;
import com.board.post.domain.Post;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        PostJpaEntity saved = postJpaRepository.save(PostPersistenceMapper.toEntity(post));
        return PostPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id).map(PostPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        postJpaRepository.deleteById(id);
    }
}
