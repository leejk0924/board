package com.board.auth.adapter.in.web.dto;

import com.board.auth.application.port.in.LoginUseCase.LoginResult;
import com.board.auth.application.port.in.ReissueTokenUseCase.ReissueResult;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn());
    }

    public static LoginResponse from(ReissueResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn());
    }
}
