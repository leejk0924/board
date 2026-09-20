package com.board.member.domain;

import java.time.LocalDateTime;

public final class Member {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String nickname;
    private final LocalDateTime createdAt;

    private Member(Long id, String email, String passwordHash, String nickname, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public static Member register(String email, String passwordHash, String nickname) {
        return new Member(null, email, passwordHash, nickname, LocalDateTime.now());
    }

    public static Member reconstitute(Long id, String email, String passwordHash, String nickname, LocalDateTime createdAt) {
        return new Member(id, email, passwordHash, nickname, createdAt);
    }

    public Member withId(Long id) {
        return new Member(id, this.email, this.passwordHash, this.nickname, this.createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
