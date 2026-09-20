package com.board.member.adapter.out.persistence;

import com.board.member.domain.Member;

final class MemberPersistenceMapper {

    private MemberPersistenceMapper() {
    }

    static Member toDomain(MemberJpaEntity entity) {
        return Member.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getNickname(),
                entity.getCreatedAt()
        );
    }

    static MemberJpaEntity toEntity(Member domain) {
        return new MemberJpaEntity(
                domain.getId(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getNickname(),
                domain.getCreatedAt()
        );
    }
}
