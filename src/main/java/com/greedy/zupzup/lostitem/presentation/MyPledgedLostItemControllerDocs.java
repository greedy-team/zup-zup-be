package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListRequest;
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
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
                                                   "count": 3,
                                                   "items": [
                                                       {
                                                           "id": 3,
                                                           "categoryId": 1,
                                                           "categoryName": "전자기기",
                                                           "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                           "schoolAreaId": 3,
                                                           "schoolAreaName": "대양 AI 센터",
                                                           "foundAreaDetail": "AI 센터 B205",
                                                           "createdAt": "2025-09-30T11:39:44.355103+09:00",
                                                           "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                       },
                                                       {
                                                           "id": 2,
                                                           "categoryId": 1,
                                                           "categoryName": "전자기기",
                                                           "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                           "schoolAreaId": 2,
                                                           "schoolAreaName": "대양 AI 센터",
                                                           "foundAreaDetail": "AI 센터 B205",
                                                           "createdAt": "2025-09-30T11:39:44.285587+09:00",
                                                           "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                       },
                                                       {
                                                           "id": 1,
                                                           "categoryId": 1,
                                                           "categoryName": "전자기기",
                                                           "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                           "schoolAreaId": 1,
                                                           "schoolAreaName": "대양 AI 센터",
                                                           "foundAreaDetail": "AI 센터 B205",
                                                           "createdAt": "2025-09-30T11:39:44.181142+09:00",
                                                           "representativeImageUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                       }
                                                   ],
                                                   "pageInfo": {
                                                       "page": 1,
                                                       "size": 10,
                                                       "totalElements": 3,
                                                       "totalPages": 1,
                                                       "hasPrev": false,
                                                       "hasNext": false
                                                   }
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
    ResponseEntity<LostItemListResponse> getMyPledgedLostItems(
            @MemberAuth LoginMember loginMember,
            @Valid @ParameterObject MyPledgedListRequest query
    );
}
