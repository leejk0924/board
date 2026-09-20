package com.board.auth;

import com.board.member.Member;
import java.time.LocalDateTime;

public record MemberResponse(Long id, String email, String nickname, LocalDateTime createdAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCreatedAt());
    }
}
