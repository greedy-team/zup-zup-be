package com.greedy.zupzup.member.application;


import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.common.fixture.MemberFixture;
import com.greedy.zupzup.member.domain.Member;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class MemberServiceTest extends ServiceUnitTest {

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("회원 조회 또는 생성)")
    class FindOrCreateMember {

        @Test
        void 이미_가입된_학번이_존재하면_회원정보를_저장하지_않고_가입된_멤버_객체를_반환해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            SejongAuthInfo authInfo = new SejongAuthInfo(member.getStudentId());

            when(memberRepository.findByStudentId(authInfo.studentId())).thenReturn(Optional.of(member));

            // when
            Member resultMember = memberService.findOrCreateMember(authInfo);

            // then
            assertThat(resultMember).isEqualTo(member);
            then(memberRepository).should().findByStudentId(authInfo.studentId());
            then(memberRepository).should(never()).save(any(Member.class));
        }

        @Test
        void 가입된_학번이_없으면_회원정보를_저장하고_가입된_멤버_객체를_반환해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            SejongAuthInfo authInfo = new SejongAuthInfo(member.getStudentId());

            when(memberRepository.findByStudentId(authInfo.studentId())).thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(member);

            // when
            Member resultMember = memberService.findOrCreateMember(authInfo);

            // then
            assertThat(resultMember).isEqualTo(member);
            then(memberRepository).should().findByStudentId(authInfo.studentId());
            then(memberRepository).should().save(any(Member.class));
        }
    }

}
