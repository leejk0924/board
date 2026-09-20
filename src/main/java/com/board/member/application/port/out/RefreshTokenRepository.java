package com.board.member.application.port.out;

import com.board.member.domain.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void deleteByMemberId(Long memberId);
}
