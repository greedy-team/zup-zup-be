package com.greedy.zupzup.auth.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthException implements ExceptionCode {

    INVALID_SEJONG_PORTAL_LOGIN_ID_PW(HttpStatus.BAD_REQUEST, "세종대학교 포털 로그인 실패", "세종대학교 인증에 실패했습니다. 아이디 비밀번호를 다시 한번 확인해 주세요."),
    SEJONG_PORTAL_LOGIN_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "세종대학교 포털 서버 통신 오류", "세종대학교 포털 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인 만료", "로그인이 만료되었습니다. 다시 로그인해주세요."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 형식", "인증 정보가 올바르지 않습니다. 다시 로그인해주세요."),
    UNAUTHENTICATED_REQUEST(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청", "로그인이 필요합니다."),
    ALREADY_REGISTERED_MEMBER(HttpStatus.BAD_REQUEST, "가입된 사용자", "이미 가입된 사용자 입니다."),
    SEJONG_VERIFICATION_EXPIRED(HttpStatus.UNAUTHORIZED, "세종대학교 인증 필요", "세종대학교 인증이 만료되었거나, 아직 인증하지 않았습니다."),
    STUDENT_ID_MISMATCH(HttpStatus.BAD_REQUEST, "인증 정보 불일치", "가입 요청된 학번과, 인증된 학번이 일치하지 않습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

}
