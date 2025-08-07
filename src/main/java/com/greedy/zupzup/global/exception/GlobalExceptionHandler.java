package com.greedy.zupzup.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 어플리케이션 로직에서 발생시킨 예외를 처리합니다.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> applicationExceptionHandler(ApplicationException ex, HttpServletRequest request) {
        return createErrorResponse(ex.getCode(), request.getRequestURI());
    }

    /**
     * 알 수 없는 오류로 인한 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(Exception ex, HttpServletRequest request) {
        log.error("예상하지 못한 예외가 발생했습니다.", ex);
        CommonException code = CommonException.INTERNAL_SERVER_ERROR;
        return createErrorResponse(code, request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(ExceptionCode code, String instance) {
        ErrorResponse errorResponse = ErrorResponse.of(code, instance);
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(errorResponse);
    }
}
