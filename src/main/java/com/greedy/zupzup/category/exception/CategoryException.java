package com.greedy.zupzup.category.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryException implements ExceptionCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없음", "요청하신 카테고리를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
