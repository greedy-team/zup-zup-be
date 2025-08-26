package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "LostItem View", description = "분실물 조회 API")
public interface LostItemSummaryControllerDocs {

    @Operation(
            summary = "분실물 전체 조회(구역별 개수)",
            description = """
        캠퍼스 **구역별 분실물 등록 건수**를 반환합니다.
        - 로그인 불필요
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요약 데이터 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    ResponseEntity<LostItemSummaryResponse> getSummary(
            @Parameter(description = "카테고리 ID(선택). 1 이상 정수", example = "3")
            @RequestParam(required = false) @Min(1) Long categoryId
    );
}
