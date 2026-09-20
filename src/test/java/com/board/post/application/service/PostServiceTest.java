package com.board.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.board.global.exception.RestApiException;
import com.board.post.application.exception.PostErrorCode;
import com.board.post.application.port.in.CreatePostUseCase.CreatePostCommand;
import com.board.post.application.port.in.DeletePostUseCase.DeletePostCommand;
import com.board.post.application.port.in.PostResult;
import com.board.post.application.port.in.UpdatePostUseCase.UpdatePostCommand;
import com.board.post.application.port.out.CommentCleanupPort;
import com.board.post.application.port.out.MemberLookupPort;
import com.board.post.application.port.out.PostDetailView;
import com.board.post.application.port.out.PostQueryRepository;
import com.board.post.application.port.out.PostRepository;
import com.board.post.domain.Post;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostQueryRepository postQueryRepository;

    @Mock
    private MemberLookupPort memberLookupPort;

    @Mock
    private CommentCleanupPort commentCleanupPort;

    private PostService service() {
        return new PostService(postRepository, postQueryRepository, memberLookupPort, commentCleanupPort);
    }

    @Test
    @DisplayName("게시글 작성: 작성자가 존재하면 게시글을 저장하고 닉네임을 포함한 결과를 반환한다")
    void createPost_success() {
        PostService sut = service();
        when(memberLookupPort.findNicknameById(1L)).thenReturn(Optional.of("alice"));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            return post.withId(10L);
        });

        PostResult result = sut.createPost(new CreatePostCommand(1L, "제목", "본문"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.authorNickname()).isEqualTo("alice");
        assertThat(result.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("게시글 작성: 작성자를 찾을 수 없으면 AUTHOR_NOT_FOUND 오류가 발생한다")
    void createPost_memberNotFound_throws() {
        PostService sut = service();
        when(memberLookupPort.findNicknameById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createPost(new CreatePostCommand(1L, "제목", "본문")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(PostErrorCode.AUTHOR_NOT_FOUND);

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 상세: 존재하지 않으면 POST_NOT_FOUND 오류가 발생한다")
    void getPost_notFound_throws() {
        PostService sut = service();
        when(postQueryRepository.findDetailById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getPost(999L))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(PostErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 상세: 존재하면 조회 결과를 반환한다")
    void getPost_success() {
        PostService sut = service();
        LocalDateTime now = LocalDateTime.now();
        when(postQueryRepository.findDetailById(1L))
                .thenReturn(Optional.of(new PostDetailView(1L, "제목", "본문", 5L, "alice", now, now)));

        PostResult result = sut.getPost(1L);

        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.authorNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("게시글 수정: 작성자가 아니면 POST_ACCESS_DENIED 오류가 발생한다")
    void updatePost_notOwner_throws() {
        PostService sut = service();
        Post post = Post.write("제목", "본문", 1L).withId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> sut.updatePost(new UpdatePostCommand(1L, 2L, "새 제목", "새 본문")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(PostErrorCode.POST_ACCESS_DENIED);

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 수정: 존재하지 않으면 POST_NOT_FOUND 오류가 발생한다")
    void updatePost_notFound_throws() {
        PostService sut = service();
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.updatePost(new UpdatePostCommand(1L, 2L, "새 제목", "새 본문")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(PostErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 삭제: 작성자 본인이면 댓글을 먼저 정리한 뒤 게시글을 삭제한다")
    void deletePost_owner_deletesCommentsThenPost() {
        PostService sut = service();
        Post post = Post.write("제목", "본문", 1L).withId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        sut.deletePost(new DeletePostCommand(1L, 1L));

        verify(commentCleanupPort).deleteAllByPostId(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("게시글 삭제: 작성자가 아니면 POST_ACCESS_DENIED 오류가 발생하고 아무것도 삭제되지 않는다")
    void deletePost_notOwner_throws() {
        PostService sut = service();
        Post post = Post.write("제목", "본문", 1L).withId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> sut.deletePost(new DeletePostCommand(1L, 2L)))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(PostErrorCode.POST_ACCESS_DENIED);

        verify(commentCleanupPort, never()).deleteAllByPostId(anyLong());
        verify(postRepository, never()).deleteById(anyLong());
    }
}
