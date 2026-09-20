package com.board.comment.adapter.out.persistence;

import com.board.comment.application.port.out.CommentView;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {

    @Query("""
            SELECT new com.board.comment.application.port.out.CommentView(
                c.id, c.content, c.memberId, m.nickname, c.postId, c.parentId, c.createdAt, c.updatedAt
            )
            FROM CommentJpaEntity c
            JOIN com.board.member.adapter.out.persistence.MemberJpaEntity m ON m.id = c.memberId
            WHERE c.postId = :postId
            ORDER BY c.createdAt ASC
            """)
    List<CommentView> findAllByPostIdWithAuthor(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentJpaEntity c WHERE c.postId = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
