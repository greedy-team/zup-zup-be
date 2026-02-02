package com.greedy.zupzup.member.application;


import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.common.fixture.MemberFixture;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.application.dto.MemberEmailUpdateCommand;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.exception.MemberException;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
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

    @Nested
    @DisplayName("이메일 정보 수정")
    class UpdateEmail {

        @Test
        void 이메일_정보를_수정하면_회원의_이메일과_알림설정이_변경되어야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            MemberEmailUpdateCommand command = new MemberEmailUpdateCommand(member.getId(), "new@example.com", true);

            given(memberRepository.getById(member.getId())).willReturn(member);
            given(memberRepository.existsByEmail("new@example.com")).willReturn(false);

            // when
            memberService.updateEmail(command);

            // then
            assertThat(member.getEmail()).isEqualTo("new@example.com");
            assertThat(member.getEmailAlertEnabled()).isTrue();
        }

        @Test
        void 존재하지_않는_회원의_이메일을_수정하려하면_예외가_발생해야_한다() {
            // given
            Long nonExistentMemberId = 999L;
            MemberEmailUpdateCommand command = new MemberEmailUpdateCommand(nonExistentMemberId, "new@example.com", true);

            given(memberRepository.getById(nonExistentMemberId))
                    .willThrow(new ApplicationException(MemberException.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> memberService.updateEmail(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(MemberException.MEMBER_NOT_FOUND.getDetail());
        }

        @Test
        void 이미_존재하는_이메일로_수정하려하면_예외가_발생해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            String duplicateEmail = "duplicate@example.com";
            MemberEmailUpdateCommand command = new MemberEmailUpdateCommand(member.getId(), duplicateEmail, true);

            given(memberRepository.getById(member.getId())).willReturn(member);
            given(memberRepository.existsByEmail(duplicateEmail)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.updateEmail(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(MemberException.DUPLICATE_EMAIL.getDetail());
        }
    }

}
