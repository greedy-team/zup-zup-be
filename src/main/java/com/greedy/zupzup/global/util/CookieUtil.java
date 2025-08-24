package com.greedy.zupzup.global.util;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    public static void setToken(String accessToken, int cookieExpirationSeconds, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(cookieExpirationSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static String extractToken(Cookie[] cookies) {
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
