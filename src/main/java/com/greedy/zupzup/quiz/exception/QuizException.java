package com.greedy.zupzup.quiz.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuizException implements ExceptionCode {

    QUIZ_ATTEMPT_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "퀴즈 시도 횟수 초과", "퀴즈 시도 횟수를 초과하여 더 이상 시도할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
