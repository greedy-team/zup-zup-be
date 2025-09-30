package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.presentation.dto.LostItemDetailViewResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListQuery;
import com.greedy.zupzup.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@Tag(name = "My Pledged LostItem", description = "내가 서약한 분실물 조회 API")
public interface MyPledgedLostItemControllerDocs {

    @Operation(
            summary = "내 서약 분실물 목록 조회",
            description = """
                    로그인한 사용자가 직접 서약한 분실물 목록을 최신순으로 조회합니다.
                    - 로그인 필수
                    - page는 1부터 시작, limit는 1~50 사이
                    """
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (기본 1)", example = "1"),
            @Parameter(name = "limit", description = "페이지 크기 (기본 20, 최대 50)", example = "10")
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemListResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                 "content": [
                                                     {
                                                         "id": 1,
                                                         "status": "PLEDGED",
                                                         "category": {
                                                             "id": 1,
                                                             "name": "전자기기",
                                                             "iconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                         },
                                                         "schoolArea": {
                                                             "id": 1,
                                                             "name": "대양 AI 센터"
                                                         },
                                                         "locationDetail": "AI 센터 B205",
                                                         "description": "검정색 아이폰 15 프로",
                                                         "imageUrls": [
                                                             "https://example.com/default-image.jpg"
                                                         ],
                                                         "depositArea": "학술정보원 2층 데스크",
                                                         "pledgedAt": "2025-09-29",
                                                         "createdAt": "2025-09-29T21:21:39.470346+09:00",
                                                         "quizRequired": true,
                                                         "quizAnswered": false,
                                                         "pledgedByMe": true
                                                     }
                                                 ],
                                                 "pageable": {
                                                     "pageNumber": 0,
                                                     "pageSize": 5,
                                                     "sort": {
                                                         "empty": true,
                                                         "sorted": false,
                                                         "unsorted": true
                                                     },
                                                     "offset": 0,
                                                     "paged": true,
                                                     "unpaged": false
                                                 },
                                                 "last": true,
                                                 "totalElements": 1,
                                                 "totalPages": 1,
                                                 "size": 5,
                                                 "number": 0,
                                                 "sort": {
                                                     "empty": true,
                                                     "sorted": false,
                                                     "unsorted": true
                                                 },
                                                 "numberOfElements": 1,
                                                 "first": true,
                                                 "empty": false
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(파라미터 범위/타입 오류)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "limit 초과(>50) 예시",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 입력값",
                                                      "status": 400,
                                                      "detail": "limit: limit는 50 이하이어야 합니다.",
                                                      "instance": "/api/lost-items/pledged"
                                                                                                                                                                              }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "파라미터 타입 불일치 예시",
                                            value = """
                                                    {
                                                      "title": "쿼리 파라미터 타입 불일치",
                                                      "status": 400,
                                                      "detail": "쿼리 파라미터 'page'는 'Integer' 타입이어야 합니다.",
                                                      "instance": "/api/lost-items/pledged"
                                                                                                                                                                         }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<Page<LostItemDetailViewResponse>> getMyPledgedLostItems(
            @ParameterObject MyPledgedListQuery query
    );
}
