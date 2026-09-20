package com.board.member.application.service;

import com.board.common.exception.RestApiException;
import com.board.member.application.exception.MemberErrorCode;
import com.board.member.application.port.in.LoginUseCase;
import com.board.member.application.port.in.ReissueTokenUseCase;
import com.board.member.application.port.in.SignUpUseCase;
import com.board.member.application.port.out.MemberRepository;
import com.board.member.application.port.out.PasswordEncoder;
import com.board.member.application.port.out.RefreshTokenRepository;
import com.board.member.application.port.out.TokenIssuer;
import com.board.member.application.port.out.TokenIssuer.IssuedToken;
import com.board.member.domain.Member;
import com.board.member.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthService implements SignUpUseCase, LoginUseCase, ReissueTokenUseCase {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    @Override
    @Transactional
    public MemberResult signUp(SignUpCommand command) {
        if (memberRepository.existsByEmail(command.email())) {
            throw new RestApiException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.register(
                command.email(),
                passwordEncoder.encode(command.rawPassword()),
                command.nickname()
        );
        Member saved = memberRepository.save(member);

        return new MemberResult(saved.getId(), saved.getEmail(), saved.getNickname(), saved.getCreatedAt());
    }

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new RestApiException(MemberErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(command.rawPassword(), member.getPasswordHash())) {
            throw new RestApiException(MemberErrorCode.INVALID_CREDENTIALS);
        }

        IssuedToken accessToken = tokenIssuer.issueAccessToken(member.getId(), member.getEmail());
        String refreshToken = issueAndStoreRefreshToken(member.getId());

        return new LoginResult(accessToken.token(), refreshToken, "Bearer", accessToken.expiresInSeconds());
    }

    @Override
    @Transactional
    public ReissueResult reissue(ReissueCommand command) {
        RefreshToken stored = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new RestApiException(MemberErrorCode.INVALID_REFRESH_TOKEN));

        if (stored.isExpired()) {
            refreshTokenRepository.deleteByMemberId(stored.getMemberId());
            throw new RestApiException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(stored.getMemberId())
                .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

        IssuedToken accessToken = tokenIssuer.issueAccessToken(member.getId(), member.getEmail());
        String rotatedRefreshToken = issueAndStoreRefreshToken(member.getId());

        return new ReissueResult(accessToken.token(), rotatedRefreshToken, "Bearer", accessToken.expiresInSeconds());
    }

    private String issueAndStoreRefreshToken(Long memberId) {
        IssuedToken issuedRefreshToken = tokenIssuer.issueRefreshToken();
        RefreshToken refreshToken = RefreshToken.issue(
                memberId, issuedRefreshToken.token(), issuedRefreshToken.expiresInSeconds()
        );
        refreshTokenRepository.save(refreshToken);
        return issuedRefreshToken.token();
    }
}
