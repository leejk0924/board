package com.board.post.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    @DisplayName("update()는 id와 작성자, 생성 시각은 유지한 채 제목/본문/수정 시각만 바뀐 새 인스턴스를 반환한다")
    void update_returnsNewInstanceWithChangedFields() {
        Post post = Post.write("제목", "본문", 1L).withId(10L);

        Post updated = post.update("새 제목", "새 본문");

        assertThat(updated).isNotSameAs(post);
        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getAuthorId()).isEqualTo(1L);
        assertThat(updated.getCreatedAt()).isEqualTo(post.getCreatedAt());
        assertThat(updated.getTitle()).isEqualTo("새 제목");
        assertThat(updated.getContent()).isEqualTo("새 본문");
        assertThat(post.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("isOwnedBy()는 작성자 id가 일치할 때만 true를 반환한다")
    void isOwnedBy() {
        Post post = Post.write("제목", "본문", 1L);

        assertThat(post.isOwnedBy(1L)).isTrue();
        assertThat(post.isOwnedBy(2L)).isFalse();
    }
}
