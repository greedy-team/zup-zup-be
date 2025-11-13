package com.greedy.zupzup.auth.application;

import com.greedy.zupzup.auth.application.dto.LoginCommand;
import com.greedy.zupzup.auth.application.dto.PortalLoginCommand;
import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.application.dto.SignupCommand;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.infrastructure.PasswordEncoder;
import com.greedy.zupzup.auth.infrastructure.SejongAuthenticator;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.common.fixture.MemberFixture;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.application.MemberService;
import com.greedy.zupzup.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class AuthServiceTest extends ServiceUnitTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;

    @Mock
    private SejongAuthenticator sejongAuthenticator;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Nested
    @DisplayName("세종대학교 인증")
    class VerifyStudent {

        @Test
        void 세종대학교_인증에_성공하면_인증된_학번과_이름을_반환해야_한다() {
            // given
            PortalLoginCommand command = new PortalLoginCommand("12345678", "portalPw");
            SejongAuthInfo authInfo = new SejongAuthInfo(12345678, "김세종");

            when(sejongAuthenticator.getStudentAuthInfo(anyString(), anyString())).thenReturn(authInfo);
            when(memberRepository.existsByStudentId(authInfo.studentId())).thenReturn(false);

            // when
            SejongAuthInfo result = authService.verifyStudent(command);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.studentId()).isEqualTo(authInfo.studentId());
                softly.assertThat(result.studentName()).isEqualTo(authInfo.studentName());
            });

            then(sejongAuthenticator).should().getStudentAuthInfo(anyString(), anyString());
            then(memberRepository).should().existsByStudentId(anyInt());
        }

        @Test
        void 이미_가입된_학번으로_인증을_하면_예외가_발생해야_한다() {
            // given
            PortalLoginCommand command = new PortalLoginCommand("12345678", "portalPw");
            SejongAuthInfo authInfo = new SejongAuthInfo(12345678, "김세종");

            when(sejongAuthenticator.getStudentAuthInfo(anyString(), anyString())).thenReturn(authInfo);
            when(memberRepository.existsByStudentId(authInfo.studentId())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.verifyStudent(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(AuthException.ALREADY_REGISTERED_MEMBER.getDetail());


            then(sejongAuthenticator).should().getStudentAuthInfo(anyString(), anyString());
            then(memberRepository).should().existsByStudentId(anyInt());
        }
    }


    @Nested
    @DisplayName("회원 가입")
    class Signup {

        @Test
        public void 회원_가입에_성공하면_회원정보를_저장하고_가입된_멤버_객체를_반환해야_한다() throws Exception {
            // given
            Member signupMember = MemberFixture.MEMBER();
            SejongAuthInfo authInfo = new SejongAuthInfo(signupMember.getStudentId(), signupMember.getName());
            SignupCommand command = new SignupCommand(authInfo, signupMember.getStudentId(), signupMember.getPassword());

            when(memberRepository.save(any(Member.class))).thenReturn(signupMember);
            when(passwordEncoder.encode(command.password())).thenReturn("hashedPassword");

            // when
            Member newMember = authService.signup(command);

            // then
            assertSoftly(softly -> {
                softly.assertThat(newMember.getName()).isEqualTo(signupMember.getName());
                softly.assertThat(newMember.getPassword()).isEqualTo("hashedPassword");
                softly.assertThat(newMember.getStudentId()).isEqualTo(signupMember.getStudentId());
            });
            then(memberRepository).should().save(any(Member.class));
            then(passwordEncoder).should().encode(command.password());
        }

        @Test
        public void 이미_가입된_학번으로_회원가입을_하면_예외가_발생해야_한다() {
            // given
            Member signupMember = MemberFixture.MEMBER();
            SejongAuthInfo authInfo = new SejongAuthInfo(signupMember.getStudentId(), signupMember.getName());
            SignupCommand command = new SignupCommand(authInfo, signupMember.getStudentId(), signupMember.getPassword());

            when(memberRepository.save(any(Member.class))).thenThrow(DataIntegrityViolationException.class);
            when(passwordEncoder.encode(command.password())).thenReturn("hashedPassword");

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(AuthException.ALREADY_REGISTERED_MEMBER.getDetail());
            then(memberRepository).should().save(any(Member.class));
            then(passwordEncoder).should().encode(command.password());
        }

        @Test
        void 인증된_학번과_가입_요청된_학번이_다르면_예외가_발생해야_한다() {
            // given
            SejongAuthInfo authInfo = new SejongAuthInfo(12345678, "김세종");
            SignupCommand command = new SignupCommand(authInfo, 87654321, "password");

            // when
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(AuthException.STUDENT_ID_MISMATCH.getDetail());

            // then
            then(memberRepository).should(never()).save(any(Member.class));
        }
    }


    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        void 회원_로그인에_성공하면_멤버_객체를_반환해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            ReflectionTestUtils.setField(member, "id", 1L);
            LoginCommand loginCommand = new LoginCommand(member.getStudentId(), member.getPassword());

            when(memberRepository.findByStudentId(member.getStudentId())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(loginCommand.password(), member.getPassword())).thenReturn(true);

            // when
            Member loginMember = authService.login(loginCommand);

            // then
            assertSoftly(softly -> {
                softly.assertThat(loginMember.getId()).isEqualTo(1L);
                softly.assertThat(loginMember.getName()).isEqualTo(member.getName());
                softly.assertThat(loginMember.getPassword()).isEqualTo(member.getPassword());
            });
            then(memberRepository).should().findByStudentId(member.getStudentId());
            then(passwordEncoder).should().matches(loginCommand.password(), member.getPassword());
        }

        @Test
        public void 주어진_아이디에_대해_가입된_학번이_없으면_예외가_발생해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            ReflectionTestUtils.setField(member, "id", 1L);
            LoginCommand loginCommand = new LoginCommand(member.getStudentId(), member.getPassword());

            when(memberRepository.findByStudentId(member.getStudentId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(loginCommand))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(AuthException.LOGIN_FAILED.getDetail());
            then(memberRepository).should().findByStudentId(member.getStudentId());
            then(passwordEncoder).should(never()).matches(anyString(), anyString());
        }

        @Test
        public void 아이디와_비밀번호가_일차하지_않으면_예외가_발생해야_한다() {
            // given
            String password = "zupzup123";
            Member member = MemberFixture.MEMBER_WITH_ENCODED_PASSWORD(password);
            ReflectionTestUtils.setField(member, "id", 1L);
            LoginCommand loginCommand = new LoginCommand(member.getStudentId(), "incorrectPassword");

            when(memberRepository.findByStudentId(member.getStudentId())).thenReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> authService.login(loginCommand))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(AuthException.LOGIN_FAILED.getDetail());
            then(memberRepository).should().findByStudentId(member.getStudentId());
            then(passwordEncoder).should().matches(loginCommand.password(), member.getPassword());
        }
    }


    @Nested
    @DisplayName("세종대학교 인증 + 로그인")
    class AuthenticateSejongAndLogin {

        @Test
        void 세종대학교_인증에_성공하면_MemberService를_호출하고_멤버_객체를_반환해야_한다() {
            // given
            Member member = MemberFixture.MEMBER();
            ReflectionTestUtils.setField(member, "id", 1L);
            PortalLoginCommand command = new PortalLoginCommand(String.valueOf(member.getStudentId()),  "portalPw");
            SejongAuthInfo authInfo = new SejongAuthInfo(member.getStudentId(), member.getName());

            when(sejongAuthenticator.getStudentAuthInfo(command.portalId(), command.portalPassword())).thenReturn(authInfo);
            when(memberService.findOrCreateMember(authInfo)).thenReturn(member);

            // when
            Member loginMember = authService.authenticateSejongAndLogin(command);

            // then
            assertSoftly(softly -> {
                softly.assertThat(loginMember.getId()).isEqualTo(1L);
                softly.assertThat(loginMember.getName()).isEqualTo(member.getName());
                softly.assertThat(loginMember.getStudentId()).isEqualTo(member.getStudentId());
            });

            then(sejongAuthenticator).should().getStudentAuthInfo(command.portalId(), command.portalPassword());
            then(memberService).should().findOrCreateMember(authInfo);
        }

    }

}
