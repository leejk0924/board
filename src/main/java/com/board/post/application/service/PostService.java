package com.board.post.application.service;

import com.board.common.exception.RestApiException;
import com.board.post.application.exception.PostErrorCode;
import com.board.post.application.port.in.CreatePostUseCase;
import com.board.post.application.port.in.DeletePostUseCase;
import com.board.post.application.port.in.GetPostListUseCase;
import com.board.post.application.port.in.GetPostUseCase;
import com.board.post.application.port.in.PostResult;
import com.board.post.application.port.in.UpdatePostUseCase;
import com.board.post.application.port.out.CommentCleanupPort;
import com.board.post.application.port.out.MemberLookupPort;
import com.board.post.application.port.out.PostDetailView;
import com.board.post.application.port.out.PostQueryRepository;
import com.board.post.application.port.out.PostRepository;
import com.board.post.application.port.out.PostSummaryView;
import com.board.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService implements
        CreatePostUseCase, UpdatePostUseCase, DeletePostUseCase, GetPostUseCase, GetPostListUseCase {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberLookupPort memberLookupPort;
    private final CommentCleanupPort commentCleanupPort;

    @Override
    @Transactional
    public PostResult createPost(CreatePostCommand command) {
        String authorNickname = memberLookupPort.findNicknameById(command.memberId())
                .orElseThrow(() -> new RestApiException(PostErrorCode.AUTHOR_NOT_FOUND));

        Post post = Post.write(command.title(), command.content(), command.memberId());
        Post saved = postRepository.save(post);

        return toResult(saved, authorNickname);
    }

    @Override
    public PostPageResult getPostList(GetPostListQuery query) {
        String keyword = StringUtils.hasText(query.keyword()) ? query.keyword() : null;
        Page<PostSummaryView> page = postQueryRepository.search(keyword, PageRequest.of(query.page(), query.size()));

        return new PostPageResult(
                page.getContent().stream().map(this::toSummaryResult).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public PostResult getPost(Long postId) {
        PostDetailView view = postQueryRepository.findDetailById(postId)
                .orElseThrow(() -> new RestApiException(PostErrorCode.POST_NOT_FOUND));

        return new PostResult(
                view.id(), view.title(), view.content(), view.authorId(), view.authorNickname(),
                view.createdAt(), view.updatedAt()
        );
    }

    @Override
    @Transactional
    public PostResult updatePost(UpdatePostCommand command) {
        Post post = getPostOrThrow(command.postId());
        validateOwner(post, command.memberId());

        Post updated = postRepository.save(post.update(command.title(), command.content()));
        String authorNickname = memberLookupPort.findNicknameById(updated.getAuthorId())
                .orElseThrow(() -> new RestApiException(PostErrorCode.AUTHOR_NOT_FOUND));

        return toResult(updated, authorNickname);
    }

    @Override
    @Transactional
    public void deletePost(DeletePostCommand command) {
        Post post = getPostOrThrow(command.postId());
        validateOwner(post, command.memberId());

        commentCleanupPort.deleteAllByPostId(command.postId());
        postRepository.deleteById(command.postId());
    }

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RestApiException(PostErrorCode.POST_NOT_FOUND));
    }

    private void validateOwner(Post post, Long memberId) {
        if (!post.isOwnedBy(memberId)) {
            throw new RestApiException(PostErrorCode.POST_ACCESS_DENIED);
        }
    }

    private PostResult toResult(Post post, String authorNickname) {
        return new PostResult(
                post.getId(), post.getTitle(), post.getContent(), post.getAuthorId(), authorNickname,
                post.getCreatedAt(), post.getUpdatedAt()
        );
    }

    private PostSummaryResult toSummaryResult(PostSummaryView view) {
        return new PostSummaryResult(
                view.id(), view.title(), view.authorNickname(), view.createdAt(), view.updatedAt(), view.commentCount()
        );
    }
}
