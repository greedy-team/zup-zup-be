package com.greedy.zupzup.member.application;

import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 학생 정보(SejongAuthInfo)를 받아 회원 찾고 생성.
     */
    @Transactional
    public Member findOrCreateMember(SejongAuthInfo studentAuthInfo) {
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
