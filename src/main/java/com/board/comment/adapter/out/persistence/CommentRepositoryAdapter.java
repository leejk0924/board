package com.board.comment.adapter.out.persistence;

import com.board.comment.application.port.out.CommentRepository;
import com.board.comment.domain.Comment;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity saved = commentJpaRepository.save(CommentPersistenceMapper.toEntity(comment));
        return CommentPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findById(id).map(CommentPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        commentJpaRepository.deleteById(id);
    }
}
