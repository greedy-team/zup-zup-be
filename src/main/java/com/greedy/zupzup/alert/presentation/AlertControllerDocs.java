package com.greedy.zupzup.alert.presentation;

import com.greedy.zupzup.alert.presentation.dto.SubscriptionResponse;
import com.greedy.zupzup.alert.presentation.dto.SubscriptionUpdateRequest;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Alert", description = "알림 및 구독 관련 API")
public interface AlertControllerDocs {

    @Operation(
            summary = "내 구독 카테고리 조회",
            description = """
                    현재 로그인한 회원이 구독 중인 카테고리 ID 목록을 조회합니다.
                    **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**
                    
                    ### 응답 예시
                    ```json
                    {
                      "categoryIds": [1, 3, 5]
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class),
                            examples = @ExampleObject(name = "구독 목록 조회 예시", value = """
                                    {
                                      "categoryIds": [1, 3, 5]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패 예시", value = """
                                    {
                                      "title": "인증되지 않은 요청",
                                      "status": 401,
                                      "detail": "로그인이 필요합니다.",
                                      "instance": "/api/alerts/subscriptions"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SubscriptionResponse> getSubscriptions(
            @Parameter(hidden = true) LoginMember loginMember
    );

    @Operation(
            summary = "구독 카테고리 수정 (전체 교체)",
            description = """
                    회원의 구독 카테고리 목록을 수정합니다.
                    **기존 구독 정보를 모두 삭제하고, 전달된 카테고리 ID 목록으로 새로 저장합니다.**
                    
                    ### 요청 예시
                    ```json
                    {
                      "categoryIds": [2, 4]
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "유효성 검증 실패 예시", value = """
                                    {
                                      "title": "유효성 검증 실패",
                                      "status": 400,
                                      "detail": "카테고리 ID 목록은 필수입니다.",
                                      "instance": "/api/alerts/subscriptions"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패 예시", value = """
                                    {
                                      "title": "인증되지 않은 요청",
                                      "status": 401,
                                      "detail": "로그인이 필요합니다.",
                                      "instance": "/api/alerts/subscriptions"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<Void> updateSubscriptions(
            @Parameter(hidden = true) LoginMember loginMember,
            @Valid @RequestBody SubscriptionUpdateRequest request
    );
}
