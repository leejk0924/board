package com.board.post.application.port.out;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryRepository {

    Optional<PostDetailView> findDetailById(Long id);

    Page<PostSummaryView> search(String keyword, Pageable pageable);
}
