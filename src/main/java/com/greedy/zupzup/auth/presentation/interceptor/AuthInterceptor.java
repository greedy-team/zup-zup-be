package com.greedy.zupzup.auth.presentation.interceptor;

import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // CORS Preflight 요청 처리 - 임시
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        String accessToken = CookieUtil.extractToken(request.getCookies());
        Long loginMemberId = jwtTokenProvider.getLoginMemberId(accessToken);

        if (loginMemberId == null) {
            throw new ApplicationException(AuthException.MEMBER_NOT_FOUND);
        }

        request.setAttribute("loginMemberId", loginMemberId);

        return true;
    }
}
