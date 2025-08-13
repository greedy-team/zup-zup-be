package com.greedy.zupzup.lostitem.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LostItemException implements ExceptionCode {

    LOST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "분실물 없음", "해당 ID의 분실물을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
