package com.board.member.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    boolean existsByEmail(String email);

    Optional<MemberJpaEntity> findByEmail(String email);
}
