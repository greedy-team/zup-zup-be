package com.greedy.zupzup.pledge.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PledgeException implements ExceptionCode {

    QUIZ_NOT_PASSED(HttpStatus.FORBIDDEN, "퀴즈 미통과", "퀴즈를 통과해야 서약할 수 있습니다."),
    CANNOT_PLEDGE_STATUS(HttpStatus.CONFLICT, "서약 불가 상태", "이미 서약되었거나 처리할 수 없는 상태의 분실물입니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
