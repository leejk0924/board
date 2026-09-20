package com.board.comment;

import com.board.global.exception.ForbiddenException;
import com.board.global.exception.InvalidRequestException;
import com.board.global.exception.NotFoundException;
import com.board.member.Member;
import com.board.member.MemberRepository;
import com.board.post.Post;
import com.board.post.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse create(Long postId, Long memberId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));
        Comment parent = resolveParent(postId, request.parentId());

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .member(member)
                .parent(parent)
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    public List<CommentResponse> getList(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId);
        }
        return commentRepository.findAllByPostIdWithMember(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse update(Long commentId, Long memberId, CommentUpdateRequest request) {
        Comment comment = getCommentOrThrow(commentId);
        validateOwner(comment, memberId);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long commentId, Long memberId) {
        Comment comment = getCommentOrThrow(commentId);
        validateOwner(comment, memberId);
        commentRepository.delete(comment);
    }

    private Comment resolveParent(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("부모 댓글을 찾을 수 없습니다. id=" + parentId));
        if (!parent.getPost().getId().equals(postId)) {
            throw new InvalidRequestException("부모 댓글이 해당 게시글에 속하지 않습니다.");
        }
        if (parent.isReply()) {
            throw new InvalidRequestException("대댓글은 한 단계까지만 작성할 수 있습니다.");
        }
        return parent;
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));
    }

    private void validateOwner(Comment comment, Long memberId) {
        if (!comment.isOwnedBy(memberId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 수정/삭제할 수 있습니다.");
        }
    }
}
