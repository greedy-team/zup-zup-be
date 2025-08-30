package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
                    - 카테고리 미지정 시 전체 카테고리 기준으로 집계됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요약 데이터 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemSummaryResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                                {
                                                    "areas": [
                                                        {
                                                            "schoolAreaId": 1,
                                                            "schoolAreaName": "세종대학교 운동장",
                                                            "lostCount": 1
                                                        },
                                                        {
                                                            "schoolAreaId": 2,
                                                            "schoolAreaName": "세종대학교 AI 센터",
                                                            "lostCount": 1
                                                        },
                                                        {
                                                            "schoolAreaId": 3,
                                                            "schoolAreaName": "세종대학교 운동장 옆 길",
                                                            "lostCount": 1
                                                        }
                                                    ]
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "categoryId가 1 미만인 경우",
                                            value = """
                                                    {
                                                      "title": "잘못된 쿼리 파라미터",
                                                      "status": 400,
                                                      "detail": "카테고리 ID는 1 이상이어야 합니다.",
                                                      "instance": "/api/lost-items/summary"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "categoryId 타입 불일치(숫자 아님)",
                                            value = """
                                                    {
                                                      "title": "잘못된 쿼리 파라미터",
                                                      "status": 400,
                                                      "detail": "쿼리 파라미터 'categoryId'는 'Long' 타입이어야 합니다.",
                                                      "instance": "/api/lost-items/summary"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<LostItemSummaryResponse> getSummary(
            @Parameter(description = "카테고리 ID(선택). 1 이상 정수", example = "3")
            @RequestParam(required = false) @Min(1) Long categoryId
    );
}
