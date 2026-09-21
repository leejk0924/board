package com.board.member.adapter.in.web;

import com.board.member.application.port.in.SignUpUseCase;
import com.board.member.application.port.in.SignUpUseCase.SignUpCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final SignUpUseCase signUpUseCase;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpCommand command = new SignUpCommand(request.email(), request.password(), request.nickname());
        return SignUpResponse.from(signUpUseCase.signUp(command));
    }
}
