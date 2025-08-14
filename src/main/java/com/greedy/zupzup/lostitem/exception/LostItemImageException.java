package com.greedy.zupzup.lostitem.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LostItemImageException implements ExceptionCode {

    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "잘못된 이미지 개수입니다.", "분실물 사진은 최소 1개 이상 3개 이하로 등록해야 합니다.")
    ;

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

}
