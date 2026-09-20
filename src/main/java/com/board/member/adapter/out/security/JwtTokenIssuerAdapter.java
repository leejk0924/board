package com.board.member.adapter.out.security;

import com.board.global.security.jwt.JwtTokenProvider;
import com.board.member.application.port.out.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenIssuerAdapter implements TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public IssuedToken issue(Long memberId, String email) {
        String token = jwtTokenProvider.generateToken(memberId, email);
        return new IssuedToken(token, jwtTokenProvider.getExpirySeconds());
    }
}
