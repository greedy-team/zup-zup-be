package com.greedy.zupzup.admin.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin - cache", description = "캐시 리프레시 관련 API (admin 전용)")
public interface AdminCacheControllerDocs {

    @Operation(summary = "학교 구역 캐시 리프레시",
            description = "전체 학교 구역 조회 캐시를 삭제하고, 다시 조회하여 캐시에 적재합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "학교 구역 캐시 리프레시 성공")
    })
    ResponseEntity<Void> refreshSchoolAreaCache();

    @Operation(summary = "카테고리 캐시 리프레시",
            description = "전체 카테고리 및 카테고리별 상세 정보(특징/옵션) 캐시를 모두 삭제하고, 전체 카테고리 목록 캐시를 다시 적재합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카테고리 캐시 리프레시 성공")
    })
    ResponseEntity<Void> refreshCategoryCache();
}
