package com.board.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.board.comment.application.exception.CommentErrorCode;
import com.board.comment.application.port.in.CommentResult;
import com.board.comment.application.port.in.CreateCommentUseCase.CreateCommentCommand;
import com.board.comment.application.port.in.DeleteCommentUseCase.DeleteCommentCommand;
import com.board.comment.application.port.in.UpdateCommentUseCase.UpdateCommentCommand;
import com.board.comment.application.port.out.CommentQueryRepository;
import com.board.comment.application.port.out.CommentRepository;
import com.board.comment.application.port.out.MemberLookupPort;
import com.board.comment.application.port.out.PostLookupPort;
import com.board.comment.domain.Comment;
import com.board.common.exception.RestApiException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentQueryRepository commentQueryRepository;

    @Mock
    private PostLookupPort postLookupPort;

    @Mock
    private MemberLookupPort memberLookupPort;

    private CommentService service() {
        return new CommentService(commentRepository, commentQueryRepository, postLookupPort, memberLookupPort);
    }

    @Test
    @DisplayName("댓글 작성: 게시글이 존재하지 않으면 POST_NOT_FOUND 오류가 발생한다")
    void createComment_postNotFound_throws() {
        CommentService sut = service();
        when(postLookupPort.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> sut.createComment(new CreateCommentCommand(1L, 1L, "내용", null)))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.POST_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성: 루트 댓글이면 정상적으로 저장된다")
    void createComment_rootComment_success() {
        CommentService sut = service();
        when(postLookupPort.existsById(1L)).thenReturn(true);
        when(memberLookupPort.findNicknameById(1L)).thenReturn(Optional.of("alice"));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return comment.withId(100L);
        });

        CommentResult result = sut.createComment(new CreateCommentCommand(1L, 1L, "내용", null));

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.authorNickname()).isEqualTo("alice");
        assertThat(result.parentId()).isNull();
    }

    @Test
    @DisplayName("댓글 작성: 부모 댓글이 이미 대댓글이면 REPLY_DEPTH_EXCEEDED 오류가 발생한다")
    void createComment_replyToReply_throws() {
        CommentService sut = service();
        when(postLookupPort.existsById(1L)).thenReturn(true);
        when(memberLookupPort.findNicknameById(1L)).thenReturn(Optional.of("alice"));
        Comment reply = Comment.write("대댓글", 1L, 2L, 10L).withId(20L);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> sut.createComment(new CreateCommentCommand(1L, 1L, "대대댓글", 20L)))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.REPLY_DEPTH_EXCEEDED);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성: 부모 댓글이 다른 게시글 소속이면 PARENT_COMMENT_MISMATCH 오류가 발생한다")
    void createComment_parentBelongsToOtherPost_throws() {
        CommentService sut = service();
        when(postLookupPort.existsById(1L)).thenReturn(true);
        when(memberLookupPort.findNicknameById(1L)).thenReturn(Optional.of("alice"));
        Comment parentOfOtherPost = Comment.write("루트", 2L, 2L, null).withId(30L);
        when(commentRepository.findById(30L)).thenReturn(Optional.of(parentOfOtherPost));

        assertThatThrownBy(() -> sut.createComment(new CreateCommentCommand(1L, 1L, "댓글", 30L)))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.PARENT_COMMENT_MISMATCH);
    }

    @Test
    @DisplayName("댓글 목록: 게시글이 존재하지 않으면 POST_NOT_FOUND 오류가 발생한다")
    void getComments_postNotFound_throws() {
        CommentService sut = service();
        when(postLookupPort.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> sut.getComments(1L))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 수정: 작성자가 아니면 COMMENT_ACCESS_DENIED 오류가 발생한다")
    void updateComment_notOwner_throws() {
        CommentService sut = service();
        Comment comment = Comment.write("내용", 1L, 1L, null).withId(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> sut.updateComment(new UpdateCommentCommand(1L, 2L, "수정")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED);
    }

    @Test
    @DisplayName("댓글 삭제: 작성자 본인이면 삭제된다")
    void deleteComment_owner_success() {
        CommentService sut = service();
        Comment comment = Comment.write("내용", 1L, 1L, null).withId(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        sut.deleteComment(new DeleteCommentCommand(1L, 1L));

        verify(commentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("댓글 삭제: 작성자가 아니면 COMMENT_ACCESS_DENIED 오류가 발생하고 삭제되지 않는다")
    void deleteComment_notOwner_throws() {
        CommentService sut = service();
        Comment comment = Comment.write("내용", 1L, 1L, null).withId(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> sut.deleteComment(new DeleteCommentCommand(1L, 2L)))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED);

        verify(commentRepository, never()).deleteById(any());
    }
}
