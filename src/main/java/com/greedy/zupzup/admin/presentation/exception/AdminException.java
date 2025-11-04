package com.greedy.zupzup.admin.presentation.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdminException implements ExceptionCode {

    FORBIDDEN_ADMIN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한 없음", "관리자 권한이 필요한 요청입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

}
