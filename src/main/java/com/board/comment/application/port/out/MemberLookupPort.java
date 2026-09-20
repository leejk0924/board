package com.board.comment.application.port.out;

import java.util.Optional;

public interface MemberLookupPort {

    Optional<String> findNicknameById(Long memberId);
}
