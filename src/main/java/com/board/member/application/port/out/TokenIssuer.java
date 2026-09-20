package com.board.member.application.port.out;

public interface TokenIssuer {

    IssuedToken issue(Long memberId, String email);

    record IssuedToken(String token, long expiresInSeconds) {
    }
}
