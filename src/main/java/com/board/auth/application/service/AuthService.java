package com.board.auth.application.service;

import com.board.auth.application.exception.AuthErrorCode;
import com.board.auth.application.port.in.LoginUseCase;
import com.board.auth.application.port.in.ReissueTokenUseCase;
import com.board.auth.application.port.out.RefreshTokenRepository;
import com.board.auth.application.port.out.TokenIssuer;
import com.board.auth.application.port.out.TokenIssuer.IssuedToken;
import com.board.auth.domain.RefreshToken;
import com.board.common.exception.RestApiException;
import com.board.member.application.port.out.MemberRepository;
import com.board.member.application.port.out.PasswordEncoder;
import com.board.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * member 모듈이 소유한 MemberRepository/PasswordEncoder 포트를 그대로 재사용한다.
 * 로그인은 본질적으로 "회원 자격 증명을 검증하는 일"이라 auth가 member의 존재를 아는 것은
 * 자연스러운 단방향 의존(auth -> member)이며, post/comment처럼 서로를 몰라야 하는 양방향 관계와는 다르다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements LoginUseCase, ReissueTokenUseCase {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new RestApiException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(command.rawPassword(), member.getPasswordHash())) {
            throw new RestApiException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        IssuedToken accessToken = tokenIssuer.issueAccessToken(member.getId(), member.getEmail());
        String refreshToken = issueAndStoreRefreshToken(member.getId());

        return new LoginResult(accessToken.token(), refreshToken, "Bearer", accessToken.expiresInSeconds());
    }

    @Override
    @Transactional
    public ReissueResult reissue(ReissueCommand command) {
        RefreshToken stored = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new RestApiException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (stored.isExpired()) {
            refreshTokenRepository.deleteByMemberId(stored.getMemberId());
            throw new RestApiException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(stored.getMemberId())
                .orElseThrow(() -> new RestApiException(AuthErrorCode.MEMBER_NOT_FOUND));

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
