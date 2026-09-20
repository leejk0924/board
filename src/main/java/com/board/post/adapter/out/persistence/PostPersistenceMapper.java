package com.board.post.adapter.out.persistence;

import com.board.post.domain.Post;

final class PostPersistenceMapper {

    private PostPersistenceMapper() {
    }

    static Post toDomain(PostJpaEntity entity) {
        return Post.reconstitute(
                entity.getId(), entity.getTitle(), entity.getContent(), entity.getMemberId(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    static PostJpaEntity toEntity(Post domain) {
        return new PostJpaEntity(
                domain.getId(), domain.getTitle(), domain.getContent(), domain.getAuthorId(),
                domain.getCreatedAt(), domain.getUpdatedAt()
        );
    }
}
