package com.greedy.zupzup.auth.application;

import com.greedy.zupzup.auth.application.dto.PortalLoginCommand;
import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.application.dto.SignupCommand;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
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

        try {
            Member newMember = Member.builder()
                    .name(sejongAuthInfo.studentName())
                    .studentId(sejongAuthInfo.studentId())
                    .password(command.password())
                    .role(Role.USER)
                    .build();

            System.out.println("가입 ㄱㄱ" + newMember.toString());
            memberRepository.save(newMember);

            return newMember;
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(AuthException.ALREADY_REGISTERED_MEMBER);
        }
    }


    /**
     * 포털 인증만으로 로그인 (데모데이 용) - 정식 출시 전에는 삭제하고, 인증+가입 절차만 열어두기
     */
    @Transactional
    public Member authenticateSejongAndLogin(PortalLoginCommand command) {
        SejongAuthInfo studentAuthInfo = sejongAuthenticator.getStudentAuthInfo(command.portalId(), command.portalPassword());
         return memberRepository.findByStudentId(studentAuthInfo.studentId())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name(studentAuthInfo.studentName())
                            .studentId(studentAuthInfo.studentId())
                            .role(Role.USER)
                            .build();
                    return memberRepository.save(newMember);
                });
    }

}
