package com.greedy.zupzup.pledge.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.pledge.presentation.dto.PledgeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Pledge", description = "분실물 찾기 서약 관련 API")
public interface PledgeControllerDocs {

    @Operation(summary = "분실물 습득 서약 생성",
            description = """
                    사용자가 분실물을 찾기 전 악용 방지를 위해 서약을 생성합니다.
                    **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "서약 생성 성공"),
            @ApiResponse(responseCode = "404", description = "요청한 분실물을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 서약이 완료되어 처리할 수 없음")
    })
    ResponseEntity<PledgeResponse> createPledge(
            @Parameter(description = "서약할 분실물", required = true, example = "12")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );
}
