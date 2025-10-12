package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostResponseView;
import com.greedy.zupzup.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Parameters({
            @Parameter(name = "page", description = "페이지(1 이상 정수)", example = "1"),
            @Parameter(name = "limit", description = "페이지 크기(1~50)", example = "10"),
            @Parameter(name = "categoryId", description = "카테고리 ID(선택)", example = "3"),
            @Parameter(name = "schoolAreaId", description = "학교 구역 ID(선택)", example = "101")
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemListResponse.class),
                            examples = @ExampleObject(
                                    name = "목록 조회 성공 예시",
                                    value = """
                                            {
                                             {
                                                   "count": 2,
                                                   "items": [
                                                       {
                                                       {
                                                           "id": 2,
                                                           "categoryId": 1,
                                                           "categoryName": "전자기기",
                                                           "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                           "schoolAreaId": 2,
                                                           "schoolAreaName": "세종대학교 AI 센터",
                                                           "foundAreaDetail": "AI 센터 B205",
                                                           "createdAt": "2025-08-30T15:16:15.831658+09:00",
                                                           "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                       },
                                                       {
                                                           "id": 1,
                                                           "categoryId": 1,
                                                           "categoryName": "전자기기",
                                                           "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                           "schoolAreaId": 1,
                                                           "schoolAreaName": "세종대학교 AI 센터",
                                                           "foundAreaDetail": "AI 센터 B205",
                                                           "createdAt": "2025-08-30T15:16:15.697167+09:00",
                                                           "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                       }
                                                   ],
                                                   "pageInfo": {
                                                       "page": 1,
                                                       "size": 10,
                                                       "totalElements": 2,
                                                       "totalPages": 1,
                                                       "hasPrev": false,
                                                       "hasNext": false
                                                   }
                                                  }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(파라미터 범위/타입 오류 등)",
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
                                                      "instance": "/api/lost-items"
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
                                                      "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "필수 파라미터 누락 예시",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 쿼리 파라미터",
                                                      "status": 400,
                                                      "detail": "필수 쿼리 파라미터 'page'(Integer)가 누락되었습니다.",
                                                      "instance": "/api/lost-items"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<LostItemListResponse> list(@ParameterObject @Valid LostItemListRequest query);

    @Operation(
            summary = "분실물 단건(간단) 조회",
            description = """
                    목록 카드에 사용하는 간단 정보(대표 이미지 포함)를 조회합니다.
                    - 로그인 불필요
                    - 상태가 REGISTERED가 아닌(PLEDGED/FOUND 등) 분실물은 조회가 제한됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "단건 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostResponseView.class),
                            examples = @ExampleObject(
                                    name = "단건 조회 성공 예시",
                                    value = """
                                            {
                                                "id": 1,
                                                "categoryId": 1,
                                                "categoryName": "전자기기",
                                                "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                "schoolAreaId": 1,
                                                "schoolAreaName": "세종대학교 AI 센터",
                                                "foundAreaDetail": "AI 센터 B205",
                                                "createdAt": "2025-08-30T15:21:22.399516+09:00",
                                                "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 제한(REGISTERED 이외 상태)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "PLEDGED/FOUND 등 접근 제한 예시",
                                    value = """
                                            {
                                              "title": "접근이 제한되었습니다.",
                                              "status": 403,
                                              "detail": "해당 분실물의 보관 정보는 열람 권한이 없습니다.",
                                              "instance": "/api/lost-items/12"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "분실물을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 ID 예시",
                                    value = """
                                            {
                                              "title": "분실물 없음",
                                              "status": 404,
                                              "detail": "해당 ID의 분실물을 찾을 수 없습니다.",
                                              "instance": "/api/lost-items/999999"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LostResponseView> getBasic(
            @Parameter(description = "조회할 분실물 ID", required = true, example = "12")
            @PathVariable Long lostItemId
    );
}
