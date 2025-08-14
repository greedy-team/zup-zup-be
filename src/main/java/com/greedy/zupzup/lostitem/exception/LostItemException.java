package com.greedy.zupzup.lostitem.exception;

import com.greedy.zupzup.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LostItemException implements ExceptionCode {

    LOST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "분실물 없음", "해당 ID의 분실물을 찾을 수 없습니다."),
    LOST_ITEM_IMAGE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "분실물 이미지 없음", "분실물에 이미지가 등록되지 않았습니다."),
    ALREADY_PLEDGED(HttpStatus.CONFLICT, "이미 서약된 물품", "분실물이 이미 서약(PLEDGED) 상태이므로, 더 이상 작업을 수행할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
