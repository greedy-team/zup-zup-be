package com.greedy.zupzup.member.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberException implements ExceptionCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없음", "해당 ID의 회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이메일 중복", "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "이메일 미등록", "이메일이 등록되어야 알림 등록이 가능합니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
