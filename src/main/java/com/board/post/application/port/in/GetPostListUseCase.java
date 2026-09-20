package com.board.post.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

public interface GetPostListUseCase {

    PostPageResult getPostList(GetPostListQuery query);

    record GetPostListQuery(String keyword, int page, int size) {
    }

    record PostSummaryResult(
            Long id,
            String title,
            String authorNickname,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long commentCount
    ) {
    }

    record PostPageResult(
            List<PostSummaryResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
    }
}
