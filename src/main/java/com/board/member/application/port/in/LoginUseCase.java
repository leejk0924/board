package com.board.member.application.port.in;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);

    record LoginCommand(String email, String rawPassword) {
    }

    record LoginResult(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    }
}
