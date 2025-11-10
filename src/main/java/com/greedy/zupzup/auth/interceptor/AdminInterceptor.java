package com.greedy.zupzup.auth.interceptor;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.util.CookieUtil;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // CORS Preflight 요청 처리
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        String accessToken = CookieUtil.extractToken(request.getCookies());
        Long loginMemberId = jwtTokenProvider.getLoginMemberId(accessToken);

        if (!memberRepository.existsByIdAndRole(loginMemberId, Role.ADMIN)) {
            throw new ApplicationException(AuthException.FORBIDDEN_ADMIN_ONLY);
        }

        LoginMember loginMember = new LoginMember(loginMemberId);
        request.setAttribute("loginMember", loginMember);

        loggingAdminAccess(request, loginMember);

        return loginMemberId != null;
    }

    private void loggingAdminAccess(HttpServletRequest request, LoginMember loginAdmin) {
        String requestId = MDC.get("request_id");
        String clientIp = request.getRemoteAddr();
        log.info("[{}] LOGIN ADMIN ID: {} | IP: {}", requestId, loginAdmin.memberId(), clientIp);
    }
}
