package com.board.member.domain;

import java.time.LocalDateTime;

public final class RefreshToken {

    private final Long id;
    private final Long memberId;
    private final String token;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    private RefreshToken(Long id, Long memberId, String token, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static RefreshToken issue(Long memberId, String token, long expiresInSeconds) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshToken(null, memberId, token, now.plusSeconds(expiresInSeconds), now);
    }

    public static RefreshToken reconstitute(
            Long id, Long memberId, String token, LocalDateTime expiresAt, LocalDateTime createdAt
    ) {
        return new RefreshToken(id, memberId, token, expiresAt, createdAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
