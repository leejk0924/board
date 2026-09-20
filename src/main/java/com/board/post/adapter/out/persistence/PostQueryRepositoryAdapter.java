package com.board.post.adapter.out.persistence;

import com.board.post.application.port.out.PostDetailView;
import com.board.post.application.port.out.PostQueryRepository;
import com.board.post.application.port.out.PostSummaryView;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostQueryRepositoryAdapter implements PostQueryRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Optional<PostDetailView> findDetailById(Long id) {
        return postJpaRepository.findDetailViewById(id);
    }

    @Override
    public Page<PostSummaryView> search(String keyword, Pageable pageable) {
        return postJpaRepository.search(keyword, pageable);
    }
}
