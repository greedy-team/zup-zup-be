package com.greedy.zupzup.category.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LostItemFeatureException implements ExceptionCode {

    INVALID_FEATURE_OPTION(HttpStatus.BAD_REQUEST, "잘못된 옵션값", "요청하신 특징에 대한 옵션값이 해당 특징에 대한 옵션이 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
