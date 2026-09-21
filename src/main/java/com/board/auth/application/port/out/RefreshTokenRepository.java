package com.board.auth.application.port.out;

import com.board.auth.domain.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void deleteByMemberId(Long memberId);
}
