package com.greedy.zupzup.auth.presentation;

import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.infrastructure.SejongAuthenticator;
import com.greedy.zupzup.auth.presentation.dto.*;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.member.domain.Member;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.when;

class AuthControllerTest extends ControllerTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String SEJONG_VERIFICATION_SESSION_COOKIE_NAME = "JSESSIONID";


    @MockitoBean
    private SejongAuthenticator sejongAuthenticator;

    @Nested
    @DisplayName("세종대학교 인증 API")
    class VerifySejong {

        @Test
        void 세종대학교_인증에_성공하면_인증_정보가_세션에_저장되고_인증된_학번을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            SejongAuthInfo authInfo = new SejongAuthInfo(studentId, "김세종");
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenReturn(authInfo);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/verify-sejong")
                    .then().log().all()
                    .extract();

            // then
            VerifiedStudentResponse response = extract.as(VerifiedStudentResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.studentId()).isEqualTo(studentId);
                softly.assertThat(response.message()).isEqualTo("세종대학교 인증에 성공했습니다.");
                softly.assertThat(extract.cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME)).isNotNull();
            });
        }
        
        @Test
        void 잘못된_세종대학교_포털_로그인_정보로_인증하면_401_UNAUTHORIZED을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            ApplicationException exceptionToThrow = new ApplicationException(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW);
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenThrow(exceptionToThrow);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);
            
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/verify-sejong")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
                softly.assertThat(response.title()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/verify-sejong");
            });
        }
        
        @Test
        void 인증에_성공했으나_해당_학번으로_이미_가입된_회원이_존재하는_경우에는_409_CONFLICT를_응답해야_한다() {
            // given
            String password = "password";
            Member givenMember = givenMember(password);

            Integer studentId = givenMember.getStudentId();   // 이미 가입된 멤버와 같은 학번으로 인증 요청
            String portalPw = "portalPw";
            SejongAuthInfo authInfo = new SejongAuthInfo(studentId, givenMember.getName());
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenReturn(authInfo);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);
            
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/verify-sejong")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(409);
                softly.assertThat(response.title()).isEqualTo(AuthException.ALREADY_REGISTERED_MEMBER.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.ALREADY_REGISTERED_MEMBER.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.ALREADY_REGISTERED_MEMBER.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/verify-sejong");
            });
        }

        @Test
        void 세종대학교_포털_로그인_서버와_통신_오류가_발생하면_503_SERVICE_UNAVAILABLE을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            ApplicationException exceptionToThrow = new ApplicationException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenThrow(exceptionToThrow);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/verify-sejong")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(503);
                softly.assertThat(response.title()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/verify-sejong");
            });
        }
    }


    @Nested
    @DisplayName("회원 가입 API")
    class Signup {

        @Test
        void 회원가입에_성공하면_발급된_억세스토큰을_쿠키에_저장하고_201_CREATED와_저장된_멤버_식별자를_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            SignupRequest signupRequest = new SignupRequest(studentId, "password");

            ExtractableResponse<Response> verifiedResponse = verifySejong(studentId);  // 세종대학교 인증
            String sessionCookie = verifiedResponse.cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME, sessionCookie) // 세션 쿠키를 포함하여 요청
                    .body(signupRequest)
                    .when()
                    .post("/api/auth/signup")
                    .then().log().all()
                    .extract();

            // then
            SignupResponse response = extract.as(SignupResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(201);
                softly.assertThat(response.message()).isEqualTo("회원가입에 성공했습니다!");
                Optional<Member> newMember = memberRepository.findById(response.memberId());
                softly.assertThat(newMember).isPresent();
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull();
            });
        }


        @Test
        void 입력한_비밀번호가_6_20자_길이의_영문_숫자_특수문자만으로_이루어지지_않은_경우_400_BAD_REQUEST를_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String password = "asd";
            SignupRequest signupRequest = new SignupRequest(studentId, password);

            ExtractableResponse<Response> verifiedResponse = verifySejong(studentId);  // 세종대학교 인증
            String sessionCookie = verifiedResponse.cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME, sessionCookie) // 세션 쿠키를 포함하여 요청
                    .body(signupRequest)
                    .when()
                    .post("/api/auth/signup")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
                softly.assertThat(response.title()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getTitle());
                softly.assertThat(response.status()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getHttpStatus().value());
                softly.assertThat(response.detail()).contains("비밀번호는 6~20자 길이의 영문, 숫자, 특수문자만 사용할 수 있습니다.");
                softly.assertThat(response.instance()).isEqualTo("/api/auth/signup");
            });
        }

        @Test
        void 인증된_학번과_가입_요청된_학번이_다르면_400_BAD_REQUEST를_응답해야_한다() {
            // given
            Integer signupStudentId = 12345678;
            Integer verifiedStudentId = 87654321;
            String password = "password";
            SignupRequest signupRequest = new SignupRequest(signupStudentId, password);

            ExtractableResponse<Response> verifiedResponse = verifySejong(verifiedStudentId);  // 세종대학교 인증
            String sessionCookie = verifiedResponse.cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(SEJONG_VERIFICATION_SESSION_COOKIE_NAME, sessionCookie) // 세션 쿠키를 포함하여 요청
                    .body(signupRequest)
                    .when()
                    .post("/api/auth/signup")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
                softly.assertThat(response.title()).isEqualTo(AuthException.STUDENT_ID_MISMATCH.getTitle());
                softly.assertThat(response.status()).isEqualTo(AuthException.STUDENT_ID_MISMATCH.getHttpStatus().value());
                softly.assertThat(response.detail()).isEqualTo(AuthException.STUDENT_ID_MISMATCH.getDetail());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/signup");
            });
        }

        @Test
        public void 세종대학교_인증을_하지_않고_가입_요청을_한_경우에는_401_UNAUTHORIZED을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String password = "password";
            SignupRequest signupRequest = new SignupRequest(studentId, password);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signupRequest)
                    .when()
                    .post("/api/auth/signup")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
                softly.assertThat(response.title()).isEqualTo(AuthException.SEJONG_VERIFICATION_EXPIRED.getTitle());
                softly.assertThat(response.status()).isEqualTo(AuthException.SEJONG_VERIFICATION_EXPIRED.getHttpStatus().value());
                softly.assertThat(response.detail()).isEqualTo(AuthException.SEJONG_VERIFICATION_EXPIRED.getDetail());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/signup");
            });
        }

        /**
         * 세종대학교 인증을 진행하는 헬퍼 메서드
         */
        private ExtractableResponse<Response> verifySejong(Integer studentId) {

            String portalPw = "portalPw";
            SejongAuthInfo authInfo = new SejongAuthInfo(studentId, "김세종");
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenReturn(authInfo);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            return RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/verify-sejong")
                    .then().log().all()
                    .extract();
        }
    }


    @Nested
    @DisplayName("로그인 API")
    class Login {

        @Test
        void 로그인에_성공하면_발급된_억세스토큰을_쿠키에_저장하고_200_OK와_저장된_멤버_식별자를_응답해야_한다() {
            // given
            String password = "password";
            Member member = givenMember(password);

            LoginRequest loginRequest = new LoginRequest(String.valueOf(member.getStudentId()), password);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then().log().all()
                    .extract();

            // then
            LoginResponse response = extract.as(LoginResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.memberId()).isEqualTo(member.getId());
                softly.assertThat(response.message()).isEqualTo("로그인에 성공했습니다.");
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull();
            });
        }

        @Test
        public void 가입되지_않은_학번으로_로그인을_요청하면_401_UNAUTHORIZED을_응답해야_한다() {
            // given
            LoginRequest loginRequest = new LoginRequest("12345678", "password");

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
                softly.assertThat(response.title()).isEqualTo(AuthException.LOGIN_FAILED.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.LOGIN_FAILED.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.LOGIN_FAILED.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/login");
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNull();
            });
        }

        @Test
        public void 가입되었지만_비밀번호가_일치하지_않으면_401_UNAUTHORIZED을_응답해야_한다() {
            // given
            String password = "requestPassword";
            Member member = givenMember("password");

            LoginRequest loginRequest = new LoginRequest(String.valueOf(member.getStudentId()), password);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
                softly.assertThat(response.title()).isEqualTo(AuthException.LOGIN_FAILED.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.LOGIN_FAILED.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.LOGIN_FAILED.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/login");
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNull();
            });
        }
    }


    @Nested
    @DisplayName("로그아웃 API")
    class Logout {

        @Test
        void 로그아웃을_하면_쿠키에_저장된_억세스토큰을_삭제하고_204_NO_CONTENT를_응답해야_한다() {
            // given
            Member member = givenMember("password");
            String accessToken = givenAccessToken(member);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                    .when()
                    .post("/api/auth/logout")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(204);
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isEmpty();
            });
        }
    }


    @Nested
    @DisplayName("세종대학교 인증 + 즉시 로그인 API")
    class PortalLogin {

        @Test
        void 세종대학교_인증에_성공하면_발급된_억세스토큰을_쿠키에_저장하고_200_OK와_저장된_멤버_식별자를_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            SejongAuthInfo authInfo = new SejongAuthInfo(studentId, "김세종");
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenReturn(authInfo);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/login/portal")
                    .then().log().all()
                    .extract();

            // then
            LoginResponse response = extract.as(LoginResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                Optional<Member> loginMember = memberRepository.findByStudentId(studentId);
                softly.assertThat(response.memberId()).isEqualTo(loginMember.get().getId());
                softly.assertThat(response.message()).isEqualTo("로그인에 성공했습니다.");
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull();
            });
        }

        @Test
        void 해당_학번으로_이미_가입된_회원이_존재하는_경우에도_발급된_억세스토큰을_쿠키에_저장하고_200_OK와_저장된_멤버_식별자를_응답해야_한다() {
            // given
            String password = "password";
            Member givenMember = givenMember(password);

            Integer studentId = givenMember.getStudentId();   // 이미 가입된 멤버와 같은 학번으로 포털 로그인 요청
            String portalPw = "portalPw";
            SejongAuthInfo authInfo = new SejongAuthInfo(studentId, givenMember.getName());
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenReturn(authInfo);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/login/portal")
                    .then().log().all()
                    .extract();

            // then
            LoginResponse response = extract.as(LoginResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                Optional<Member> loginMember = memberRepository.findByStudentId(studentId);
                softly.assertThat(response.memberId()).isEqualTo(loginMember.get().getId());
                softly.assertThat(response.message()).isEqualTo("로그인에 성공했습니다.");
                softly.assertThat(extract.cookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull();
            });
        }

        @Test
        void 잘못된_세종대학교_포털_로그인_정보로_로그인하면_401_UNAUTHORIZED을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            ApplicationException exceptionToThrow = new ApplicationException(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW);
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenThrow(exceptionToThrow);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/login/portal")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
                softly.assertThat(response.title()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/login/portal");
            });
        }

        @Test
        void 세종대학교_포털_로그인_서버와_통신_오류가_발생하면_503_SERVICE_UNAVAILABLE을_응답해야_한다() {
            // given
            Integer studentId = 12345678;
            String portalPw = "portalPw";
            ApplicationException exceptionToThrow = new ApplicationException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            when(sejongAuthenticator.getStudentAuthInfo(String.valueOf(studentId), portalPw)).thenThrow(exceptionToThrow);

            PortalLoginRequest verifyRequest = new PortalLoginRequest(String.valueOf(studentId), portalPw);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(verifyRequest)
                    .when()
                    .post("/api/auth/login/portal")
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(503);
                softly.assertThat(response.title()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getDetail());
                softly.assertThat(response.status()).isEqualTo(AuthException.SEJONG_PORTAL_LOGIN_FAILED.getHttpStatus().value());
                softly.assertThat(response.instance()).isEqualTo("/api/auth/login/portal");
            });
        }
    }

}
