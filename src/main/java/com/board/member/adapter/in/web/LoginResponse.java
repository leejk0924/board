package com.board.member.adapter.in.web;

import com.board.member.application.port.in.LoginUseCase.LoginResult;
import com.board.member.application.port.in.ReissueTokenUseCase.ReissueResult;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn());
    }

    public static LoginResponse from(ReissueResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn());
    }
}
