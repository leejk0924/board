package com.board.member.adapter.out.security;

import com.board.common.security.jwt.JwtTokenProvider;
import com.board.member.application.port.out.TokenIssuer;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuerAdapter implements TokenIssuer {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTE_LENGTH = 64;

    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshExpirationSeconds;

    public JwtTokenIssuerAdapter(
            JwtTokenProvider jwtTokenProvider,
            @Value("${jwt.refresh-expiration-seconds}") long refreshExpirationSeconds
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    @Override
    public IssuedToken issueAccessToken(Long memberId, String email) {
        String token = jwtTokenProvider.generateToken(memberId, email);
        return new IssuedToken(token, jwtTokenProvider.getExpirySeconds());
    }

    @Override
    public IssuedToken issueRefreshToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return new IssuedToken(token, refreshExpirationSeconds);
    }
}
