package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDepositAreaResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDetailViewResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "LostItem Detail View", description = "분실물 상세/이미지 조회 API")
public interface LostItemDetailViewControllerDocs {

    @Operation(
            summary = "분실물 상세 조회(보관 장소 포함)",
            description = """
                    분실물의 상세 정보를 조회합니다. (보관 장소 포함)
                    - **로그인 필수**
                    - 퀴즈가 필요한 카테고리의 경우: **서약 완료 + 퀴즈 통과** 사용자만 접근 가능
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemDetailViewResponse.class),
                            examples = @ExampleObject(
                                    name = "상세 조회 성공 예시",
                                    value = """
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
                                                      "name": "세종대학교 AI 센터"
                                                  },
                                                  "locationDetail": "AI 센터 B205",
                                                  "description": "검정색 아이폰 15 프로",
                                                  "imageUrls": [
                                                      "https://example.com/default-image.jpg"
                                                  ],
                                                  "depositArea": "학술정보원 2층 데스크",
                                                  "pledgedAt": "2025-08-30",
                                                  "createdAt": "2025-08-30T14:39:10.216778+09:00",
                                                  "quizRequired": true,
                                                  "quizAnswered": true,
                                                  "pledgedByMe": true
                                              }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요(액세스 토큰 없음/만료/무효)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족(서약 미완료/퀴즈 미통과 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "접근 제한(권한 부족)",
                                    value = """
                                            {
                                              "title": "접근이 제한되었습니다.",
                                              "status": 403,
                                              "detail": "해당 분실물의 보관 정보는 열람 권한이 없습니다.",
                                              "instance": "/api/lost-items/101/detail"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "분실물 없음",
                                    value = """
                                            {
                                              "title": "분실물 없음",
                                              "status": 404,
                                              "detail": "해당 ID의 분실물을 찾을 수 없습니다.",
                                              "instance": "/api/lost-items/999999/detail"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LostItemDetailViewResponse> getDetail(
            @Parameter(description = "상세 조회할 분실물 ID", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );

    @Operation(
            summary = "분실물 사진 + 상세 정보 (퀴즈 이후, 서약 전)",
            description = """
                    분실물의 원본 이미지들과 상세 정보를 제공합니다.
                    - **로그인 필수**
                    - 퀴즈가 필요한 카테고리의 경우: **퀴즈 통과** 사용자만 접근 가능
                    - 퀴즈가 필요 없는 카테고리의 경우: 로그인만 하면 접근 가능
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 이미지/정보 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemImageResponse.class),
                            examples = @ExampleObject(
                                    name = "상세 이미지/정보 조회 성공 예시",
                                    value = """
                                            {
                                                  "id": 1,
                                                  "categoryId": 1,
                                                  "categoryName": "전자기기",
                                                  "categoryIconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png",
                                                  "schoolAreaId": 1,
                                                  "schoolAreaName": "세종대학교 AI 센터",
                                                  "foundAreaDetail": "AI 센터 B205",
                                                  "description": "검정색 아이폰 15 프로",
                                                  "imageUrls": [
                                                      "https://example.com/default-image.jpg"
                                                  ],
                                                  "createdAt": "2025-08-30T14:47:16.256973+09:00"
                                              }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족(퀴즈 미통과 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "접근 제한(퀴즈 미통과)",
                                    value = """
                                            {
                                              "title": "접근이 제한되었습니다.",
                                              "status": 403,
                                              "detail": "해당 분실물의 보관 정보는 열람 권한이 없습니다.",
                                              "instance": "/api/lost-items/101/image"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LostItemImageResponse> getImagesAfterQuiz(
            @Parameter(description = "이미지/상세를 조회할 분실물 ID", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );

    @Operation(
            summary = "분실물 보관 위치 조회",
            description = """
                    분실물의 보관 위치만 조회합니다.
                    - **로그인 필수**
                    - 퀴즈가 필요한 카테고리의 경우: **서약 완료 + 퀴즈 통과** 사용자만 접근 가능
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "보관 위치 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = LostItemDepositAreaResponse.class),
                            examples = @ExampleObject(
                                    name = "보관 위치 조회 성공 예시",
                                    value = """
                                            {
                                                "depositArea": "학술 정보원 2층 데스크"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족(서약/퀴즈 조건 미충족)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "접근 제한(권한 부족)",
                                    value = """
                                            {
                                              "title": "접근이 제한되었습니다.",
                                              "status": 403,
                                              "detail": "해당 분실물의 보관 정보는 열람 권한이 없습니다.",
                                              "instance": "/api/lost-items/101/deposit-area"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LostItemDepositAreaResponse> getDepositArea(
            @Parameter(description = "보관 위치를 조회할 분실물 ID", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );
}
