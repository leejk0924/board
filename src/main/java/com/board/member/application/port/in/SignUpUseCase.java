package com.board.member.application.port.in;

import java.time.LocalDateTime;

public interface SignUpUseCase {

    MemberResult signUp(SignUpCommand command);

    record SignUpCommand(String email, String rawPassword, String nickname) {
    }

    record MemberResult(Long id, String email, String nickname, LocalDateTime createdAt) {
    }
}
