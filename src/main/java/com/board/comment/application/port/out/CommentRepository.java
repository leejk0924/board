package com.board.comment.application.port.out;

import com.board.comment.domain.Comment;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    void deleteById(Long id);
}
