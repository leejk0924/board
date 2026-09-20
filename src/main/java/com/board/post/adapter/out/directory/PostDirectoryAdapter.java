package com.board.post.adapter.out.directory;

import com.board.comment.application.port.out.PostLookupPort;
import com.board.post.adapter.out.persistence.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * comment 모듈이 정의한 PostLookupPort의 구현체. post 모듈만 자신의 영속성 내부(PostJpaRepository)에
 * 접근할 수 있으므로, 이 어댑터가 그 접근을 캡슐화해서 comment 모듈에 제공한다.
 */
@Component
@RequiredArgsConstructor
public class PostDirectoryAdapter implements PostLookupPort {

    private final PostJpaRepository postJpaRepository;

    @Override
    public boolean existsById(Long postId) {
        return postJpaRepository.existsById(postId);
    }
}
