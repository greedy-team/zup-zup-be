package com.greedy.zupzup.category.presentation;

import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import io.swagger.v3.oas.annotations.Operation;
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
                모든 카테고리를 ID 오름차순으로 반환합니다.
                - 로그인 불필요
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    })
    ResponseEntity<CategoriesResponse> categories();

    @Operation(
            summary = "카테고리의 특징/옵션 조회",
            description = """
                특정 카테고리의 특징(Feature)과 각 Feature의 옵션을 함께 반환합니다.
                - 로그인 불필요
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특징/옵션 조회 성공"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    ResponseEntity<CategoryFeaturesResponse> getCategoryFeatures(
            @Parameter(description = "특징/옵션을 조회할 카테고리 ID", required = true, example = "1")
            @PathVariable Long categoryId
    );
}
