package com.board.post.adapter.out.persistence;

import com.board.post.application.port.out.PostDetailView;
import com.board.post.application.port.out.PostSummaryView;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {

    @Query("""
            SELECT new com.board.post.application.port.out.PostDetailView(
                p.id, p.title, p.content, p.memberId, m.nickname, p.createdAt, p.updatedAt
            )
            FROM PostJpaEntity p
            JOIN com.board.member.adapter.out.persistence.MemberJpaEntity m ON m.id = p.memberId
            WHERE p.id = :id
            """)
    Optional<PostDetailView> findDetailViewById(@Param("id") Long id);

    @Query(value = """
            SELECT new com.board.post.application.port.out.PostSummaryView(
                p.id, p.title, m.nickname, p.createdAt, p.updatedAt, COUNT(c)
            )
            FROM PostJpaEntity p
            JOIN com.board.member.adapter.out.persistence.MemberJpaEntity m ON m.id = p.memberId
            LEFT JOIN com.board.comment.adapter.out.persistence.CommentJpaEntity c ON c.postId = p.id
            WHERE (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))
            GROUP BY p.id, p.title, m.nickname, p.createdAt, p.updatedAt
            ORDER BY p.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM PostJpaEntity p
            WHERE (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<PostSummaryView> search(@Param("keyword") String keyword, Pageable pageable);
}
