package com.board.member.adapter.in.web;

import com.board.member.application.port.in.LoginUseCase.LoginResult;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.tokenType(), result.expiresIn());
    }
}
