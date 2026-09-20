package com.board.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.board.common.exception.RestApiException;
import com.board.member.application.exception.MemberErrorCode;
import com.board.member.application.port.in.LoginUseCase.LoginCommand;
import com.board.member.application.port.in.LoginUseCase.LoginResult;
import com.board.member.application.port.in.ReissueTokenUseCase.ReissueCommand;
import com.board.member.application.port.in.ReissueTokenUseCase.ReissueResult;
import com.board.member.application.port.in.SignUpUseCase.MemberResult;
import com.board.member.application.port.in.SignUpUseCase.SignUpCommand;
import com.board.member.application.port.out.MemberRepository;
import com.board.member.application.port.out.PasswordEncoder;
import com.board.member.application.port.out.RefreshTokenRepository;
import com.board.member.application.port.out.TokenIssuer;
import com.board.member.application.port.out.TokenIssuer.IssuedToken;
import com.board.member.domain.Member;
import com.board.member.domain.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenIssuer tokenIssuer;

    private MemberAuthService service() {
        return new MemberAuthService(memberRepository, refreshTokenRepository, passwordEncoder, tokenIssuer);
    }

    @Test
    @DisplayName("회원가입: 이메일이 중복되지 않으면 비밀번호를 해시해 저장하고 결과를 반환한다")
    void signUp_success() {
        MemberAuthService sut = service();
        when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return member.withId(1L);
        });

        MemberResult result = sut.signUp(new SignUpCommand("user@example.com", "password123", "nick"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.nickname()).isEqualTo("nick");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    @DisplayName("회원가입: 이메일이 이미 존재하면 DUPLICATE_EMAIL 오류가 발생하고 저장하지 않는다")
    void signUp_duplicateEmail_throws() {
        MemberAuthService sut = service();
        when(memberRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> sut.signUp(new SignUpCommand("dup@example.com", "password123", "nick")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("로그인: 이메일과 비밀번호가 일치하면 액세스/리프레시 토큰을 발급하고 리프레시 토큰을 저장한다")
    void login_success() {
        MemberAuthService sut = service();
        Member member = Member.reconstitute(1L, "user@example.com", "hashed-password", "nick", LocalDateTime.now());
        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(tokenIssuer.issueAccessToken(1L, "user@example.com")).thenReturn(new IssuedToken("access-token", 3600L));
        when(tokenIssuer.issueRefreshToken()).thenReturn(new IssuedToken("refresh-token", 1209600L));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResult result = sut.login(new LoginCommand("user@example.com", "password123"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(3600L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
        assertThat(captor.getValue().getToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("로그인: 존재하지 않는 이메일이면 INVALID_CREDENTIALS 오류가 발생한다")
    void login_memberNotFound_throws() {
        MemberAuthService sut = service();
        when(memberRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.login(new LoginCommand("nobody@example.com", "password123")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인: 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 오류가 발생한다")
    void login_wrongPassword_throws() {
        MemberAuthService sut = service();
        Member member = Member.reconstitute(1L, "user@example.com", "hashed-password", "nick", LocalDateTime.now());
        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> sut.login(new LoginCommand("user@example.com", "wrong")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.INVALID_CREDENTIALS);

        verify(tokenIssuer, never()).issueAccessToken(any(), anyString());
    }

    @Test
    @DisplayName("토큰 재발급: 유효한 리프레시 토큰이면 액세스/리프레시 토큰을 새로 발급한다(로테이션)")
    void reissue_success() {
        MemberAuthService sut = service();
        RefreshToken stored = RefreshToken.issue(1L, "old-refresh-token", 1209600L);
        Member member = Member.reconstitute(1L, "user@example.com", "hashed-password", "nick", LocalDateTime.now());
        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(stored));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(tokenIssuer.issueAccessToken(1L, "user@example.com")).thenReturn(new IssuedToken("new-access-token", 3600L));
        when(tokenIssuer.issueRefreshToken()).thenReturn(new IssuedToken("new-refresh-token", 1209600L));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReissueResult result = sut.reissue(new ReissueCommand("old-refresh-token"));

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("토큰 재발급: 존재하지 않는 리프레시 토큰이면 INVALID_REFRESH_TOKEN 오류가 발생한다")
    void reissue_tokenNotFound_throws() {
        MemberAuthService sut = service();
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.reissue(new ReissueCommand("unknown")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("토큰 재발급: 만료된 리프레시 토큰이면 INVALID_REFRESH_TOKEN 오류가 발생하고 토큰을 삭제한다")
    void reissue_expiredToken_throwsAndDeletes() {
        MemberAuthService sut = service();
        RefreshToken expired = RefreshToken.issue(1L, "expired-token", -1L);
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> sut.reissue(new ReissueCommand("expired-token")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenRepository).deleteByMemberId(1L);
    }
}
