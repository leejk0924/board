package com.board.comment.adapter.in.web;

import com.board.comment.application.port.in.CreateCommentUseCase;
import com.board.comment.application.port.in.CreateCommentUseCase.CreateCommentCommand;
import com.board.comment.application.port.in.DeleteCommentUseCase;
import com.board.comment.application.port.in.DeleteCommentUseCase.DeleteCommentCommand;
import com.board.comment.application.port.in.GetCommentListUseCase;
import com.board.comment.application.port.in.UpdateCommentUseCase;
import com.board.comment.application.port.in.UpdateCommentUseCase.UpdateCommentCommand;
import com.board.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;

    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CreateCommentCommand command = new CreateCommentCommand(postId, member.id(), request.content(), request.parentId());
        return CommentResponse.from(createCommentUseCase.createComment(command));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> getList(@PathVariable Long postId) {
        return getCommentListUseCase.getComments(postId).stream().map(CommentResponse::from).toList();
    }

    @PatchMapping("/api/comments/{commentId}")
    public CommentResponse update(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        UpdateCommentCommand command = new UpdateCommentCommand(commentId, member.id(), request.content());
        return CommentResponse.from(updateCommentUseCase.updateComment(command));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember member, @PathVariable Long commentId) {
        deleteCommentUseCase.deleteComment(new DeleteCommentCommand(commentId, member.id()));
    }
}
