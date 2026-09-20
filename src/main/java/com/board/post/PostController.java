package com.board.post;

import com.board.global.common.PageResponse;
import com.board.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return postService.create(member.id(), request);
    }

    @GetMapping
    public PageResponse<PostSummaryResponse> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.getList(keyword, pageable);
    }

    @GetMapping("/{postId}")
    public PostResponse getDetail(@PathVariable Long postId) {
        return postService.getDetail(postId);
    }

    @PatchMapping("/{postId}")
    public PostResponse update(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return postService.update(postId, member.id(), request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember member, @PathVariable Long postId) {
        postService.delete(postId, member.id());
    }
}
