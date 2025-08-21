package com.greedy.zupzup.auth.presentation;

import com.greedy.zupzup.auth.application.AuthService;
import com.greedy.zupzup.auth.application.SejongAuthenticator;
import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.auth.presentation.dto.*;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.util.CookieUtil;
import com.greedy.zupzup.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String SEJONG_VERIFICATION_INFO_SESSION_KEY = "sejongAuthInfo";

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/verify-student")
    public ResponseEntity<VerifiedStudentResponse> verifyStudent(@RequestBody @Valid PortalLoginRequest portalLoginRequest, HttpServletRequest httpRequest) {
        SejongAuthInfo sejongAuthInfo = authService.verifyStudent(portalLoginRequest.toCommand());

        HttpSession session = httpRequest.getSession();
        session.setAttribute(SEJONG_VERIFICATION_INFO_SESSION_KEY, sejongAuthInfo);

        return ResponseEntity.ok(VerifiedStudentResponse.from(sejongAuthInfo.studentId()));
    }


    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest signupRequest,
                                                 HttpServletRequest httpRequest, HttpServletResponse response) {
        HttpSession session = httpRequest.getSession(false);
        SejongAuthInfo sejongAuthInfo = checkSessionAndGetSejongAuthInfo(session);

        Member signupMember = authService.signup(signupRequest.toCommand(sejongAuthInfo));

        // 세종대학교 인증 세션 종료
        session.invalidate();
        setAccessToken(response, signupMember);

        return ResponseEntity.created(URI.create("/members/" + signupMember.getId()))
                .body(SignupResponse.from(signupMember));
    }


    public SejongAuthInfo checkSessionAndGetSejongAuthInfo(HttpSession session) {
        if (session == null || session.getAttribute(SEJONG_VERIFICATION_INFO_SESSION_KEY) == null) {
            throw new ApplicationException(AuthException.SEJONG_VERIFICATION_EXPIRED);
        }
        return (SejongAuthInfo) session.getAttribute(SEJONG_VERIFICATION_INFO_SESSION_KEY);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        Member loginMember = authService.login(loginRequest.toCommand());
        setAccessToken(response, loginMember);
        return ResponseEntity.ok(LoginResponse.from(loginMember));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        CookieUtil.setToken("", 0, response);
        return ResponseEntity.ok().build();
    }


    /**
     * 포털 인증만으로 로그인 (데모데이 용) - 정식 출시 전에는 삭제하고, 인증+가입 절차만 열어두기
     */
    @PostMapping("/login/portal")
    public ResponseEntity<Void> portalLogin(@RequestBody @Valid PortalLoginRequest request, HttpServletResponse response) {
        Member loginMember = authService.authenticateSejongAndLogin(request.toCommand());
        setAccessToken(response, loginMember);
        return ResponseEntity.ok().build();
    }

    private void setAccessToken(HttpServletResponse response, Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        CookieUtil.setToken(accessToken, jwtTokenProvider.accessExpiration, response);
    }

}
