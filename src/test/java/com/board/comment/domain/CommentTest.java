package com.board.comment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    @DisplayName("parentId가 없으면 루트 댓글이고, 있으면 대댓글이다")
    void isReply() {
        Comment root = Comment.write("루트", 1L, 1L, null);
        Comment reply = Comment.write("대댓글", 1L, 1L, 5L);

        assertThat(root.isReply()).isFalse();
        assertThat(reply.isReply()).isTrue();
    }

    @Test
    @DisplayName("update()는 id/postId/authorId/parentId/생성시각을 유지한 채 내용만 바뀐 새 인스턴스를 반환한다")
    void update_returnsNewInstanceWithChangedContent() {
        Comment comment = Comment.write("원본", 1L, 1L, null).withId(10L);

        Comment updated = comment.update("수정된 내용");

        assertThat(updated).isNotSameAs(comment);
        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getPostId()).isEqualTo(1L);
        assertThat(updated.getAuthorId()).isEqualTo(1L);
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        assertThat(comment.getContent()).isEqualTo("원본");
    }

    @Test
    @DisplayName("isOwnedBy()는 작성자 id가 일치할 때만 true를 반환한다")
    void isOwnedBy() {
        Comment comment = Comment.write("내용", 1L, 1L, null);

        assertThat(comment.isOwnedBy(1L)).isTrue();
        assertThat(comment.isOwnedBy(2L)).isFalse();
    }
}
