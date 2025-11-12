package com.greedy.zupzup.auth.application;

import com.greedy.zupzup.auth.application.dto.LoginCommand;
import com.greedy.zupzup.auth.application.dto.PortalLoginCommand;
import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.application.dto.SignupCommand;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.infrastructure.PasswordEncoder;
import com.greedy.zupzup.auth.infrastructure.SejongAuthenticator;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.application.MemberService;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SejongAuthenticator sejongAuthenticator;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;


    public SejongAuthInfo verifyStudent(PortalLoginCommand command) {
        SejongAuthInfo studentAuthInfo = sejongAuthenticator.getStudentAuthInfo(command.portalId(), command.portalPassword());

        if (memberRepository.existsByStudentId(studentAuthInfo.studentId())) {
            throw new ApplicationException(AuthException.ALREADY_REGISTERED_MEMBER);
        }
        return studentAuthInfo;
    }


    @Transactional
    public Member signup(SignupCommand command) {
        SejongAuthInfo sejongAuthInfo = command.verifiedInfo();

        if (!sejongAuthInfo.studentId().equals(command.studentId())) {
            throw new ApplicationException(AuthException.STUDENT_ID_MISMATCH);
        }
        // 비밀번호 암호화 (해싱 Alg)
        String hashedPassword = passwordEncoder.encode(command.password());

        try {
            Member newMember = Member.builder()
                    .name(sejongAuthInfo.studentName())
                    .studentId(sejongAuthInfo.studentId())
                    .password(hashedPassword)
                    .role(Role.USER)
                    .build();

            memberRepository.save(newMember);

            return newMember;
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(AuthException.ALREADY_REGISTERED_MEMBER);
        }
    }


    public Member login(LoginCommand command) {
        Member loginMember = memberRepository.findByStudentId(command.studentId())
                .orElseThrow(() -> new ApplicationException(AuthException.LOGIN_FAILED));

        if (!passwordEncoder.matches(command.password(), loginMember.getPassword())) {
            throw new ApplicationException(AuthException.LOGIN_FAILED);
        }

        return loginMember;
    }


    /**
     * 포털 인증만으로 로그인 Tx 분리
     */
    public Member authenticateSejongAndLogin(PortalLoginCommand command) {
        SejongAuthInfo studentAuthInfo = sejongAuthenticator.getStudentAuthInfo(command.portalId(), command.portalPassword());
        return memberService.findOrCreateMember(studentAuthInfo);
    }

}
