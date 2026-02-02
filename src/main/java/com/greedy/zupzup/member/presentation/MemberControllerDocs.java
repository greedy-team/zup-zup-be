package com.greedy.zupzup.member.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.member.presentation.dto.MemberEmailUpdateRequest;
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

@Tag(name = "Member", description = "회원 관련 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "이메일 및 알림 설정 수정",
            description = """
                    회원의 이메일 주소와 알림 수신 여부를 수정합니다.
                    **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**
                    
                    ### 요청 예시
                    ```json
                    {
                      "email": "test@example.com",
                      "emailAlertEnabled": true
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (이메일 형식 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "유효성 검증 실패 예시", value = """
                                    {
                                      "title": "유효성 검증 실패",
                                      "status": 400,
                                      "detail": "올바른 이메일 형식이 아닙니다.",
                                      "instance": "/api/members/me/email"
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
                                      "instance": "/api/members/me/email"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "회원 없음 예시", value = """
                                    {
                                      "title": "회원을 찾을 수 없음",
                                      "status": 404,
                                      "detail": "해당 ID의 회원을 찾을 수 없습니다.",
                                      "instance": "/api/members/me/email"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<Void> updateEmail(
            @Parameter(hidden = true) LoginMember loginMember,
            @Valid @RequestBody MemberEmailUpdateRequest request
    );
}
