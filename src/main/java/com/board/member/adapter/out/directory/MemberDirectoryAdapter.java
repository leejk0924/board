package com.board.member.adapter.out.directory;

import com.board.member.adapter.out.persistence.MemberJpaEntity;
import com.board.member.adapter.out.persistence.MemberJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * post/comment 모듈이 정의한 MemberLookupPort를 함께 구현하는 공용 조회 어댑터.
 * 각 모듈은 자신에게 필요한 좁은 포트 인터페이스만 알고, 실제 구현은 member 모듈이 제공한다.
 */
@Component
@RequiredArgsConstructor
public class MemberDirectoryAdapter implements
        com.board.post.application.port.out.MemberLookupPort,
        com.board.comment.application.port.out.MemberLookupPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<String> findNicknameById(Long memberId) {
        return memberJpaRepository.findById(memberId).map(MemberJpaEntity::getNickname);
    }
}
