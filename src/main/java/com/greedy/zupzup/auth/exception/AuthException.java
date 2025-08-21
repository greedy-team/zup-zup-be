package com.greedy.zupzup.auth.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthException implements ExceptionCode {


    INVALID_SEJONG_PORTAL_LOGIN_ID_PW(HttpStatus.BAD_REQUEST, "세종대학교 포털 로그인 실패", "세종대학교 학생 인증에 실패했습니다. 아이디 비밀번호를 다시한번 확인해 주세요."),
    SEJONG_PORTAL_LOGIN_FILED(HttpStatus.SERVICE_UNAVAILABLE, "세종대학교 포털 서버 통신 오류", "세종대학교 포털 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인 만료", "로그인이 만료 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "access token 형식 오류", "access token이 유효하지 않습니다."),
    UNAUTHENTICATED_REQUEST(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청", "로그인이 필요합니다."),
    ;


    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
