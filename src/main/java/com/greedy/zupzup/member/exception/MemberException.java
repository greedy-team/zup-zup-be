package com.greedy.zupzup.member.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberException implements ExceptionCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없음", "해당 ID의 회원을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}

