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
import com.board.member.application.port.in.SignUpUseCase.MemberResult;
import com.board.member.application.port.in.SignUpUseCase.SignUpCommand;
import com.board.member.application.port.out.MemberRepository;
import com.board.member.application.port.out.PasswordEncoder;
import com.board.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberService service() {
        return new MemberService(memberRepository, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입: 이메일이 중복되지 않으면 비밀번호를 해시해 저장하고 결과를 반환한다")
    void signUp_success() {
        MemberService sut = service();
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
        MemberService sut = service();
        when(memberRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> sut.signUp(new SignUpCommand("dup@example.com", "password123", "nick")))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode").isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
