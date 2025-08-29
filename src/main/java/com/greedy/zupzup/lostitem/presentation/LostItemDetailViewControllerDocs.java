package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDepositAreaResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDetailViewResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                    분실물의 상세 정보(보관 장소 포함)를 조회 합니다.
                    - **로그인 필수**
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요(액세스 토큰 없음/만료/무효)"),
            @ApiResponse(responseCode = "403", description = "권한 부족(서약 미완료/퀴즈 미통과 등)"),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음")
    })
    ResponseEntity<LostItemDetailViewResponse> getDetail(
            @Parameter(description = "상세 조회할 분실물 ID", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );

    @Operation(
            summary = "분실물 사진 + 상세 정보 (퀴즈 이후, 서약 전)",
            description = """
                    분실물의 원본 이미지들과 상세 정보를 제공합니다. (보관장소/서약일 제외)
                    - **로그인 필수**
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 이미지/정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요(액세스 토큰 없음/만료/무효)"),
            @ApiResponse(responseCode = "403", description = "권한 부족(퀴즈 미통과 등)"),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음")
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
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "보관 위치 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요(액세스 토큰 없음/만료/무효)"),
            @ApiResponse(responseCode = "403", description = "권한 부족(서약/퀴즈 조건 미충족)"),
            @ApiResponse(responseCode = "404", description = "분실물을 찾을 수 없음")
    })
    ResponseEntity<LostItemDepositAreaResponse> getDepositArea(
            @Parameter(description = "보관 위치를 조회할 분실물 ID", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );
}
