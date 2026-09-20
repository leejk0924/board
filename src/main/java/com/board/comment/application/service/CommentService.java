package com.board.comment.application.service;

import com.board.comment.application.port.in.CommentResult;
import com.board.comment.application.port.in.CreateCommentUseCase;
import com.board.comment.application.port.in.DeleteCommentUseCase;
import com.board.comment.application.port.in.GetCommentListUseCase;
import com.board.comment.application.port.in.UpdateCommentUseCase;
import com.board.comment.application.port.out.CommentQueryRepository;
import com.board.comment.application.port.out.CommentRepository;
import com.board.comment.application.port.out.CommentView;
import com.board.comment.application.port.out.MemberLookupPort;
import com.board.comment.application.port.out.PostLookupPort;
import com.board.comment.application.exception.CommentErrorCode;
import com.board.comment.domain.Comment;
import com.board.common.exception.RestApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService implements
        CreateCommentUseCase, GetCommentListUseCase, UpdateCommentUseCase, DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final PostLookupPort postLookupPort;
    private final MemberLookupPort memberLookupPort;

    @Override
    @Transactional
    public CommentResult createComment(CreateCommentCommand command) {
        if (!postLookupPort.existsById(command.postId())) {
            throw new RestApiException(CommentErrorCode.POST_NOT_FOUND);
        }
        String authorNickname = memberLookupPort.findNicknameById(command.memberId())
                .orElseThrow(() -> new RestApiException(CommentErrorCode.AUTHOR_NOT_FOUND));

        validateParent(command.postId(), command.parentId());

        Comment comment = Comment.write(command.content(), command.postId(), command.memberId(), command.parentId());
        Comment saved = commentRepository.save(comment);

        return toResult(saved, authorNickname);
    }

    @Override
    public List<CommentResult> getComments(Long postId) {
        if (!postLookupPort.existsById(postId)) {
            throw new RestApiException(CommentErrorCode.POST_NOT_FOUND);
        }
        return commentQueryRepository.findAllByPostIdWithAuthor(postId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional
    public CommentResult updateComment(UpdateCommentCommand command) {
        Comment comment = getCommentOrThrow(command.commentId());
        validateOwner(comment, command.memberId());

        Comment updated = commentRepository.save(comment.update(command.content()));
        String authorNickname = memberLookupPort.findNicknameById(updated.getAuthorId())
                .orElseThrow(() -> new RestApiException(CommentErrorCode.AUTHOR_NOT_FOUND));

        return toResult(updated, authorNickname);
    }

    @Override
    @Transactional
    public void deleteComment(DeleteCommentCommand command) {
        Comment comment = getCommentOrThrow(command.commentId());
        validateOwner(comment, command.memberId());
        commentRepository.deleteById(command.commentId());
    }

    private void validateParent(Long postId, Long parentId) {
        if (parentId == null) {
            return;
        }
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new RestApiException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));
        if (!parent.getPostId().equals(postId)) {
            throw new RestApiException(CommentErrorCode.PARENT_COMMENT_MISMATCH);
        }
        if (parent.isReply()) {
            throw new RestApiException(CommentErrorCode.REPLY_DEPTH_EXCEEDED);
        }
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RestApiException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateOwner(Comment comment, Long memberId) {
        if (!comment.isOwnedBy(memberId)) {
            throw new RestApiException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }
    }

    private CommentResult toResult(Comment comment, String authorNickname) {
        return new CommentResult(
                comment.getId(), comment.getContent(), comment.getAuthorId(), authorNickname,
                comment.getPostId(), comment.getParentId(), comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }

    private CommentResult toResult(CommentView view) {
        return new CommentResult(
                view.id(), view.content(), view.authorId(), view.authorNickname(),
                view.postId(), view.parentId(), view.createdAt(), view.updatedAt()
        );
    }
}
