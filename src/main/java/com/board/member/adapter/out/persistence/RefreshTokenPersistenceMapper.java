package com.board.member.adapter.out.persistence;

import com.board.member.domain.RefreshToken;

final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(), entity.getMemberId(), entity.getToken(), entity.getExpiresAt(), entity.getCreatedAt()
        );
    }

    static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        return new RefreshTokenJpaEntity(
                domain.getId(), domain.getMemberId(), domain.getToken(), domain.getExpiresAt(), domain.getCreatedAt()
        );
    }
}
