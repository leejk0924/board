package com.board.auth.adapter.in.web;

import com.board.auth.adapter.in.web.dto.LoginRequest;
import com.board.auth.adapter.in.web.dto.LoginResponse;
import com.board.auth.adapter.in.web.dto.ReissueRequest;
import com.board.auth.application.port.in.LoginUseCase;
import com.board.auth.application.port.in.LoginUseCase.LoginCommand;
import com.board.auth.application.port.in.ReissueTokenUseCase;
import com.board.auth.application.port.in.ReissueTokenUseCase.ReissueCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;

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
