package com.board.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
