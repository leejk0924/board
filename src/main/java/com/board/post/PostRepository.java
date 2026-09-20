package com.board.post;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<Post> findWithMemberById(Long id);

    @Query(value = """
            SELECT new com.board.post.PostSummaryResponse(
                p.id, p.title, m.nickname, p.createdAt, p.updatedAt, COUNT(c)
            )
            FROM Post p
            JOIN p.member m
            LEFT JOIN Comment c ON c.post = p
            WHERE (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))
            GROUP BY p.id, p.title, m.nickname, p.createdAt, p.updatedAt
            ORDER BY p.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM Post p
            WHERE (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<PostSummaryResponse> search(@Param("keyword") String keyword, Pageable pageable);
}
