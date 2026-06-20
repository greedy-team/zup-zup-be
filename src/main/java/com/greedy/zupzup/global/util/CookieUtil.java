package com.greedy.zupzup.global.util;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CookieUtil {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    private final String cookieDomain;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public CookieUtil(@Value("${jwt.cookie.domain}") String cookieDomain,
                      @Value("${jwt.cookie.secure}") boolean cookieSecure,
                      @Value("${jwt.cookie.samesite}") String cookieSameSite) {
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    public void setToken(String accessToken, int cookieExpirationSeconds, HttpServletResponse response) {
        ResponseCookie cookie = createCookie(accessToken, cookieExpirationSeconds);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie createCookie(String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        // 도메인 설정이 있을 경우에만 적용 (.zupzup.com 등)
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new ApplicationException(AuthException.UNAUTHENTICATED_REQUEST);
        }
        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new ApplicationException(AuthException.UNAUTHENTICATED_REQUEST);
    }
}
