package com.board.comment.domain;

import java.time.LocalDateTime;

public final class Comment {

    private final Long id;
    private final String content;
    private final Long postId;
    private final Long authorId;
    private final Long parentId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Comment(
            Long id, String content, Long postId, Long authorId, Long parentId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id = id;
        this.content = content;
        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment write(String content, Long postId, Long authorId, Long parentId) {
        LocalDateTime now = LocalDateTime.now();
        return new Comment(null, content, postId, authorId, parentId, now, now);
    }

    public static Comment reconstitute(
            Long id, String content, Long postId, Long authorId, Long parentId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return new Comment(id, content, postId, authorId, parentId, createdAt, updatedAt);
    }

    public Comment withId(Long id) {
        return new Comment(id, this.content, this.postId, this.authorId, this.parentId, this.createdAt, this.updatedAt);
    }

    public Comment update(String content) {
        return new Comment(this.id, content, this.postId, this.authorId, this.parentId, this.createdAt, LocalDateTime.now());
    }

    public boolean isOwnedBy(Long memberId) {
        return this.authorId.equals(memberId);
    }

    public boolean isReply() {
        return this.parentId != null;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Long getParentId() {
        return parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
