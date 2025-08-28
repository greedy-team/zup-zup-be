package com.greedy.zupzup.schoolarea.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SchoolAreaException implements ExceptionCode {

    SCHOOL_AREA_OUT_OF_BOUND(HttpStatus.NOT_FOUND, "유효하지 않은 구역", "요청하신 좌표는 세종대학교를 벗어났습니다."),
    SCHOOL_AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "학교 구역을 찾을 수 없음", "요청하신 구역이 존재하지 않습니다.")
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
