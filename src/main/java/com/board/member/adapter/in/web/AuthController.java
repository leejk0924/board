package com.board.member.adapter.in.web;

import com.board.member.application.port.in.LoginUseCase;
import com.board.member.application.port.in.LoginUseCase.LoginCommand;
import com.board.member.application.port.in.ReissueTokenUseCase;
import com.board.member.application.port.in.ReissueTokenUseCase.ReissueCommand;
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
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpCommand command = new SignUpCommand(request.email(), request.password(), request.nickname());
        return SignUpResponse.from(signUpUseCase.signUp(command));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());
        return LoginResponse.from(loginUseCase.login(command));
    }

    @PostMapping("/reissue")
    public LoginResponse reissue(@Valid @RequestBody ReissueRequest request) {
        ReissueCommand command = new ReissueCommand(request.refreshToken());
        return LoginResponse.from(reissueTokenUseCase.reissue(command));
    }
}
