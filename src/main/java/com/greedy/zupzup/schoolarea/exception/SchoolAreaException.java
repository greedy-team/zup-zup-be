package com.greedy.zupzup.schoolarea.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SchoolAreaException implements ExceptionCode {

    SCHOOL_AREA_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 구역", "해당 좌표는 학교 범위를 벗어났습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDetail() {
        return detail;
    }

}
