package com.board.comment.adapter.out.cleanup;

import com.board.comment.adapter.out.persistence.CommentJpaRepository;
import com.board.post.application.port.out.CommentCleanupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * post 모듈이 정의한 CommentCleanupPort의 구현체. 게시글 삭제 시 딸린 댓글(대댓글 포함)을
 * 벌크 삭제하기 위해 comment 모듈의 영속성에 접근한다.
 */
@Component
@RequiredArgsConstructor
public class CommentCleanupAdapter implements CommentCleanupPort {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public void deleteAllByPostId(Long postId) {
        commentJpaRepository.deleteAllByPostId(postId);
    }
}
