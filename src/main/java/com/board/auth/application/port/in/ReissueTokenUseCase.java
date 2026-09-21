package com.board.auth.application.port.in;

public interface ReissueTokenUseCase {

    ReissueResult reissue(ReissueCommand command);

    record ReissueCommand(String refreshToken) {
    }

    record ReissueResult(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    }
}
