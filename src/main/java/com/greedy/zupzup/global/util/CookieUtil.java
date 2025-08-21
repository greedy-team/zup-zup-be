package com.greedy.zupzup.global.util;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    public static void setToken(String accessToken, int cookieExpirationSeconds, HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieExpirationSeconds);
        response.addCookie(cookie);
    }

    public static String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new ApplicationException(AuthException.UNAUTHENTICATED_REQUEST);
        }
        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                System.out.println("쿠키 값 " + cookie.getValue());
                return cookie.getValue();
            }
        }
        throw new ApplicationException(AuthException.UNAUTHENTICATED_REQUEST);
    }
}
