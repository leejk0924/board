package com.board.member.adapter.out.persistence;

import com.board.member.application.port.out.RefreshTokenRepository;
import com.board.member.domain.RefreshToken;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        refreshTokenJpaRepository.deleteByMemberId(refreshToken.getMemberId());
        RefreshTokenJpaEntity saved = refreshTokenJpaRepository.save(RefreshTokenPersistenceMapper.toEntity(refreshToken));
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token).map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        refreshTokenJpaRepository.deleteByMemberId(memberId);
    }
}
