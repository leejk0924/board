package com.board.post.adapter.in.web;

import com.board.common.PageResponse;
import com.board.common.security.AuthenticatedMember;
import com.board.post.application.port.in.CreatePostUseCase;
import com.board.post.application.port.in.CreatePostUseCase.CreatePostCommand;
import com.board.post.application.port.in.DeletePostUseCase;
import com.board.post.application.port.in.DeletePostUseCase.DeletePostCommand;
import com.board.post.application.port.in.GetPostListUseCase;
import com.board.post.application.port.in.GetPostListUseCase.GetPostListQuery;
import com.board.post.application.port.in.GetPostListUseCase.PostPageResult;
import com.board.post.application.port.in.GetPostUseCase;
import com.board.post.application.port.in.UpdatePostUseCase;
import com.board.post.application.port.in.UpdatePostUseCase.UpdatePostCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final GetPostListUseCase getPostListUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PostCreateRequest request
    ) {
        CreatePostCommand command = new CreatePostCommand(member.id(), request.title(), request.content());
        return PostResponse.from(createPostUseCase.createPost(command));
    }

    @GetMapping
    public PageResponse<PostSummaryResponse> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        PostPageResult result = getPostListUseCase.getPostList(new GetPostListQuery(keyword, page, size));
        return new PageResponse<>(
                result.content().stream().map(PostSummaryResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }

    @GetMapping("/{postId}")
    public PostResponse getDetail(@PathVariable Long postId) {
        return PostResponse.from(getPostUseCase.getPost(postId));
    }

    @PatchMapping("/{postId}")
    public PostResponse update(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        UpdatePostCommand command = new UpdatePostCommand(postId, member.id(), request.title(), request.content());
        return PostResponse.from(updatePostUseCase.updatePost(command));
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember member, @PathVariable Long postId) {
        deletePostUseCase.deletePost(new DeletePostCommand(postId, member.id()));
    }
}
