package com.board.post.domain;

import java.time.LocalDateTime;

public final class Post {

    private final Long id;
    private final String title;
    private final String content;
    private final Long authorId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Post(Long id, String title, String content, Long authorId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post write(String title, String content, Long authorId) {
        LocalDateTime now = LocalDateTime.now();
        return new Post(null, title, content, authorId, now, now);
    }

    public static Post reconstitute(
            Long id, String title, String content, Long authorId, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return new Post(id, title, content, authorId, createdAt, updatedAt);
    }

    public Post withId(Long id) {
        return new Post(id, this.title, this.content, this.authorId, this.createdAt, this.updatedAt);
    }

    public Post update(String title, String content) {
        return new Post(this.id, title, content, this.authorId, this.createdAt, LocalDateTime.now());
    }

    public boolean isOwnedBy(Long memberId) {
        return this.authorId.equals(memberId);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
