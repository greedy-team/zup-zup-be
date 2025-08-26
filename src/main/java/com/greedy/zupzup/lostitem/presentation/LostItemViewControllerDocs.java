package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.presentation.dto.LostItemListQuery;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "LostItem View", description = "분실물 조회 API")
public interface LostItemViewControllerDocs {

    @Operation(
            summary = "분실물 목록 조회",
            description = """
            카테고리/구역 필터와 페이징으로 분실물 목록을 조회합니다.
            - 로그인 불필요
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    ResponseEntity<LostItemListResponse> list(@ParameterObject @Valid LostItemListQuery query);

    @Operation(
            summary = "분실물 단건(간단) 조회",
            description = """
            목록 카드에 사용하는 간단 정보(대표 이미지 포함)를 조회합니다.
            - 로그인 불필요
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "단건 조회 성공"),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음")
    })
    ResponseEntity<LostItemViewResponse> getBasic(@Parameter(description = "조회할 분실물 ID", required = true, example = "101")
                                                  @PathVariable Long lostItemId);
}
