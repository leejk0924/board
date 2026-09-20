package com.board.member.application.port.out;

public interface TokenIssuer {

    IssuedToken issueAccessToken(Long memberId, String email);

    IssuedToken issueRefreshToken();

    record IssuedToken(String token, long expiresInSeconds) {
    }
}
