package com.greedy.zupzup.auth.presentation;

import com.greedy.zupzup.auth.application.AuthService;
import com.greedy.zupzup.auth.application.SejongAuthenticator;
import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.auth.presentation.dto.PortalLoginRequest;
import com.greedy.zupzup.auth.presentation.dto.SignupRequest;
import com.greedy.zupzup.auth.presentation.dto.VerifiedStudentResponse;
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
        String accessToken = jwtTokenProvider.createAccessToken(loginMember);
        CookieUtil.setToken(accessToken, jwtTokenProvider.accessExpiration, response);
        return ResponseEntity.ok().build();
    }



}
