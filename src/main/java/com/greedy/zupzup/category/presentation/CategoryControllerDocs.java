package com.greedy.zupzup.category.presentation;

import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import com.greedy.zupzup.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Category", description = "카테고리/특징 조회 API")
public interface CategoryControllerDocs {

    @Operation(
            summary = "카테고리 전체 조회",
            description = """
                    모든 카테고리를 **ID 오름차순**으로 반환합니다.
                    - 로그인 불필요
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = CategoriesResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                                "categories": [
                                                    {
                                                        "id": 1,
                                                        "name": "전자기기",
                                                        "iconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                    },
                                                    {
                                                        "id": 2,
                                                        "name": "지갑",
                                                        "iconUrl": "https://cdn-icons-png.flaticon.com/128/519/519184.png"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "알 수 없는 오류",
                                    value = """
                                            {
                                              "title": "알 수 없는 오류",
                                              "status": 500,
                                              "detail": "서버 내부에 알 수 없는 오류가 발생했습니다. 관리자에게 문의 하세요.",
                                              "instance": "/api/categories"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<CategoriesResponse> categories();

    @Operation(
            summary = "카테고리의 특징/옵션 조회",
            description = """
                    특정 카테고리의 **특징(Feature)** 과 각 Feature의 **옵션**을 함께 반환합니다.
                    - 로그인 불필요
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "특징/옵션 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = CategoryFeaturesResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            "categoryId": 1,
                                                 "categoryName": "전자기기",
                                                 "features": [
                                                     {
                                                         "id": 1,
                                                         "name": "브랜드",
                                                         "quizQuestion": "어떤 브랜드의 제품인가요?",
                                                         "options": [
                                                             {
                                                                 "id": 1,
                                                                 "optionValue": "삼성"
                                                             },
                                                             {
                                                                 "id": 2,
                                                                 "optionValue": "애플"
                                                             },
                                                             {
                                                                 "id": 3,
                                                                 "optionValue": "LG"
                                                             },
                                                             {
                                                                 "id": 4,
                                                                 "optionValue": "기타"
                                                             }
                                                         ]
                                                     },
                                                     {
                                                         "id": 2,
                                                         "name": "색상",
                                                         "quizQuestion": "제품의 색상은 무엇인가요?",
                                                         "options": [
                                                             {
                                                                 "id": 5,
                                                                 "optionValue": "블랙"
                                                             },
                                                             {
                                                                 "id": 6,
                                                                 "optionValue": "화이트"
                                                             },
                                                             {
                                                                 "id": 7,
                                                                 "optionValue": "실버"
                                                             },
                                                             {
                                                                 "id": 8,
                                                                 "optionValue": "골드"
                                                             },
                                                             {
                                                                 "id": 9,
                                                                 "optionValue": "기타"
                                                             }
                                                         ]
                                                     }
                                                 ]
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "카테고리를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 카테고리 ID",
                                    value = """
                                            {
                                              "title": "카테고리를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청하신 카테고리가 존재하지 않습니다.",
                                              "instance": "/api/categories/9999/features"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 경로 파라미터(타입 불일치 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "categoryId 타입 불일치",
                                    value = """
                                            {
                                              "title": "쿼리 파라미터 타입 불일치",
                                              "status": 400,
                                              "detail": "쿼리 파라미터 'categoryId'는 'Long' 타입이어야 합니다.",
                                              "instance": "/api/categories/abc/features"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "알 수 없는 오류",
                                    value = """
                                            {
                                              "title": "알 수 없는 오류",
                                              "status": 500,
                                              "detail": "서버 내부에 알 수 없는 오류가 발생했습니다. 관리자에게 문의 하세요.",
                                              "instance": "/api/categories/1/features"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<CategoryFeaturesResponse> getCategoryFeatures(
            @Parameter(description = "특징/옵션을 조회할 카테고리 ID", required = true, example = "1")
            @PathVariable Long categoryId
    );
}
