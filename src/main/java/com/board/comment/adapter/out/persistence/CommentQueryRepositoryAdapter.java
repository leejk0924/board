package com.board.comment.adapter.out.persistence;

import com.board.comment.application.port.out.CommentQueryRepository;
import com.board.comment.application.port.out.CommentView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentQueryRepositoryAdapter implements CommentQueryRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public List<CommentView> findAllByPostIdWithAuthor(Long postId) {
        return commentJpaRepository.findAllByPostIdWithAuthor(postId);
    }
}
