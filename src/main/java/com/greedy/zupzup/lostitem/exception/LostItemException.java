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
    ALREADY_PLEDGED(HttpStatus.CONFLICT, "이미 서약된 물품", "분실물이 이미 서약(PLEDGED) 상태이므로, 더 이상 작업을 수행할 수 없습니다."),
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.", "해당 분실물은 조회가 제한됩니다."),
    ACCESS_FORBIDDEN_PLEDGED(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.", "이미 서약 진행 중인 분실물로, 조회가 제한됩니다."),
    ACCESS_FORBIDDEN_FOUND(HttpStatus.FORBIDDEN, "접근이 제한되었습니다.", "이미 주인이 찾아간 분실물로, 조회가 제한됩니다."),
    FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY(HttpStatus.BAD_REQUEST, "분실물 특징 입력 누락", "분실물 카테고리가 '기타'가 아닌 경우, 필수 특징값이 모두 입력되어야 합니다."),
    REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "분실물 등록 실패", "분실물 저장 중 예상치 못한 오류가 발생했습니다."),
    PLEDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "서약 정보 없음", "해당 분실물에 대한 서약 정보가 존재하지 않습니다."),
    PLEDGE_NOT_BY_THIS_USER(HttpStatus.FORBIDDEN, "권한 없음", "해당 서약은 현재 사용자에 의해 만들어진 것이 아닙니다."),
    INVALID_STATUS_FOR_PLEDGE_CANCEL(HttpStatus.BAD_REQUEST, "서약 취소 불가 상태", "현재 상태에서는 서약을 취소할 수 없습니다."),
    INVALID_STATUS_FOR_PLEDGE_COMPLETE(HttpStatus.BAD_REQUEST, "습득 완료 처리 불가 상태", "현재 상태에서는 분실물을 습득 완료 처리할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;
}
