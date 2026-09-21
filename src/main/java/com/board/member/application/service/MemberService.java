package com.board.member.application.service;

import com.board.common.exception.RestApiException;
import com.board.member.application.exception.MemberErrorCode;
import com.board.member.application.port.in.SignUpUseCase;
import com.board.member.application.port.out.MemberRepository;
import com.board.member.application.port.out.PasswordEncoder;
import com.board.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements SignUpUseCase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
}
