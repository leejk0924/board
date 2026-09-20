package com.board.comment.adapter.out.persistence;

import com.board.comment.domain.Comment;

final class CommentPersistenceMapper {

    private CommentPersistenceMapper() {
    }

    static Comment toDomain(CommentJpaEntity entity) {
        return Comment.reconstitute(
                entity.getId(), entity.getContent(), entity.getPostId(), entity.getMemberId(), entity.getParentId(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    static CommentJpaEntity toEntity(Comment domain) {
        return new CommentJpaEntity(
                domain.getId(), domain.getContent(), domain.getPostId(), domain.getAuthorId(), domain.getParentId(),
                domain.getCreatedAt(), domain.getUpdatedAt()
        );
    }
}
