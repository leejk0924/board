package com.board.post;

import com.board.comment.CommentRepository;
import com.board.global.common.PageResponse;
import com.board.global.exception.ForbiddenException;
import com.board.global.exception.NotFoundException;
import com.board.member.Member;
import com.board.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .member(member)
                .build();

        return PostResponse.from(postRepository.save(post));
    }

    public PageResponse<PostSummaryResponse> getList(String keyword, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword : null;
        return PageResponse.from(postRepository.search(normalizedKeyword, pageable));
    }

    public PostResponse getDetail(Long postId) {
        Post post = postRepository.findWithMemberById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse update(Long postId, Long memberId, PostUpdateRequest request) {
        Post post = getPostOrThrow(postId);
        validateOwner(post, memberId);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long postId, Long memberId) {
        Post post = getPostOrThrow(postId);
        validateOwner(post, memberId);
        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    private Post getPostOrThrow(Long postId) {
        return postRepository.findWithMemberById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));
    }

    private void validateOwner(Post post, Long memberId) {
        if (!post.isOwnedBy(memberId)) {
            throw new ForbiddenException("본인이 작성한 게시글만 수정/삭제할 수 있습니다.");
        }
    }
}
