package com.board.member.adapter.in.web.dto;

import com.board.member.application.port.in.SignUpUseCase.MemberResult;
import java.time.LocalDateTime;

public record SignUpResponse(Long id, String email, String nickname, LocalDateTime createdAt) {

    public static SignUpResponse from(MemberResult result) {
        return new SignUpResponse(result.id(), result.email(), result.nickname(), result.createdAt());
    }
}
