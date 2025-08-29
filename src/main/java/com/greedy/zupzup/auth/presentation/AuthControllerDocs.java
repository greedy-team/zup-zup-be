package com.greedy.zupzup.auth.presentation;

import com.greedy.zupzup.auth.presentation.dto.*;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "세종대학교 인증/가입 및 회원 로그인/로그아웃 관련 API")
public interface AuthControllerDocs {


    @Operation(summary = "세종대학교 인증",
            description = "세종대학교 포털 아이디와 비밀번호를 통해 재학생임을 인증합니다. 성공 시, 서버 세션에 인증 정보가 저장됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세종대학교 포털 인증에 성공하고, 서버 세션에 인증 정보가 저장된 경우",
                    content = @Content(
                            schema = @Schema(implementation = VerifiedStudentResponse.class),
                            examples = @ExampleObject(
                                    name = "세종대학교 인증 성공 예시",
                                    value = """
                                            {
                                                "studentId": 20011222,
                                                "message": "세종대학교 인증에 성공했습니다."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {

                                    @ExampleObject(
                                            name = "필수 요청 필드가 누락된 경우",
                                            value = """
                                                    {
                                                        "title": "유효하지 않은 입력값",
                                                        "status": 400,
                                                        "detail": "portalPassword: 세종대학교 포털 로그인 비밀번호를 입력해 주세요., portalId: 세종대학교 포털 로그인 학번을 입력해 주세요.",
                                                        "instance": "/api/auth/verify-sejong"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "Body에 포함된 JSON 데이터를 파싱에 실패 하거나 JSON 형식이 잘못된 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 요청 본문",
                                                        "status": 400,
                                                        "detail": "요청 본문의 형식이 잘못되었습니다.",
                                                        "instance": "/api/auth/verify-sejong"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "401", description = "제출된 portalId와 portalPassword가 실제 포털 정보와 일치하지 않아 인증에 실패한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "세종대학교 포털 인증에 실패한 경우",
                                    value = """
                                            {
                                                "title": "세종대학교 포털 로그인 실패",
                                                "status": 401,
                                                "detail": "세종대학교 인증에 실패했습니다. 아이디 비밀번호를 다시 한번 확인해 주세요.",
                                                "instance": "/api/auth/verify-sejong"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "409", description = "인증에 성공했으나, 해당 학번으로 이미 서비스에 가입된 회원이 존재하는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이미 가입된 사용자 예시",
                                    value = """
                                            {
                                                "title": "가입된 사용자",
                                                "status": 409,
                                                "detail": "이미 가입된 사용자 입니다.",
                                                "instance": "/api/auth/verify-sejong"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "503", description = "세종대학교 포털 서버 자체의 문제나 네트워크 오류로 인해 인증 시도가 불가능한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "포털 인증 실패 예시",
                                    value = """
                                            {
                                                "title": "세종대학교 포털 서버 통신 오류",
                                                "status": 503,
                                                "detail": "세종대학교 포털 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                                                "instance": "/api/auth/verify-sejong"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<VerifiedStudentResponse> verifySejong(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "세종대학교 포털 인증 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PortalLoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "정상 요청 예시",
                                            value = """
                                                    {
                                                        "portalId": "2001111 (본인 포탈 로그인 아이디)",
                                                        "portalPassword": "asdasdasd (본인 포탈 로그인 PW)"
                                                    }
                                                    """
                                    )
                            }
                    )
            ) @RequestBody @Valid PortalLoginRequest portalLoginRequest,
            @Parameter(hidden = true) HttpServletRequest httpRequest

    );


    @Operation(summary = "회원가입",
            description = "세종대학교 학생 인증 후, 추가 정보를 입력하여 회원가입을 완료합니다. 학생 인증을 먼저 완료해야 합니다.",
            security = @SecurityRequirement(name = "sejongSessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입에 성공하고, 새로운 회원 리소스가 성공적으로 생성된 경우",
                    content = @Content(
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    value = """
                                            {
                                                "memberId": 2,
                                                "message": "회원가입에 성공했습니다!"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "가입 요청된 password가 서버에서 정의한 유효성 규칙(예: 길이, 형식)을 통과하지 못한 경우",
                                            value = """
                                                    {
                                                        "title": "유효하지 않은 입력값",
                                                        "status": 400,
                                                        "detail": "password: 비밀번호는 6~20자 길이의 영문, 숫자, 특수문자만 사용할 수 있습니다.",
                                                        "instance": "/api/auth/signup"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "필수 입력 필드가 누락된 경우 ",
                                            value = """
                                                    {
                                                        "title": "유효하지 않은 입력값",
                                                        "status": 400,
                                                        "detail": "password: 비밀번호는 필수입니다.",
                                                        "instance": "/api/auth/signup"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "가입 요청된 학번과 세종대학교 포털 인증한 학변이 다른 경우",
                                            value = """
                                                    {
                                                        "title": "인증 정보 불일치",
                                                        "status": 400,
                                                        "detail": "가입 요청된 학번과, 인증된 학번이 일치하지 않습니다.",
                                                        "instance": "/api/auth/signup"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "Body에 포함된 JSON 데이터를 파싱에 실패 하거나 JSON 형식이 잘못된 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 요청 본문",
                                                        "status": 400,
                                                        "detail": "요청 본문의 형식이 잘못되었습니다.",
                                                        "instance": "/api/auth/verify-sejong"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "401", description = "요청에 포함된 세션(JSESSIONID)이 없거나 만료되어, 세종대학교 인증 상태를 확인할 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "세종대학교 인증 정보 없음 또는 만료", value = """
                                    {
                                        "title": "세종대학교 인증 필요",
                                        "status": 401,
                                        "detail": "세종대학교 인증이 만료되었거나, 아직 인증하지 않았습니다.",
                                        "instance": "/api/auth/signup"
                                    }
                                    """
                            )
                    )
            ),
    })
    ResponseEntity<SignupResponse> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "줍줍 회원 가입 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "정상 요청 예시",
                                            value = """
                                                    {
                                                        "studentId": 20011111,
                                                        "password": "asdasda@123"
                                                    }
                                                    """
                                    )
                            }
                    )
            ) @RequestBody @Valid SignupRequest signupRequest,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );


    @Operation(summary = "줍줍 로그인",
            description = "줍줍 아이디와 비밀번호를 사용하여 로그인합니다. 성공 시, Access Token이 쿠키에 설정됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인에 성공하고, access_token이 쿠키에 정상적으로 설정된 경우",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "줍줍 로그인 성공 예시",
                                    value = """
                                            {
                                                "memberId": 2,
                                                "message": "로그인에 성공했습니다."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {

                                    @ExampleObject(
                                            name = "필수 입력 필드가 누락된 경우",
                                            value = """
                                                    {
                                                        "title": "유효하지 않은 입력값",
                                                        "status": 400,
                                                        "detail": "studentId: 줍줍 로그인 학번을 입력해 주세요.",
                                                        "instance": "/api/auth/login"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "Body에 포함된 JSON 데이터를 파싱에 실패 하거나 JSON 형식이 잘못된 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 요청 본문",
                                                        "status": 400,
                                                        "detail": "요청 본문의 형식이 잘못되었습니다.",
                                                        "instance": "/api/auth/login"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "401", description = "존재하지 않는 studentId로 로그인을 시도했거나, password가 일치하지 않는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "줍줍 로그인 실패 예시",
                                    value = """
                                            {
                                                "title": "로그인 실패",
                                                "status": 401,
                                                "detail": "아이디 또는 패스워드가 일치하지 않습니다.",
                                                "instance": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "줍줍 로그인 가입 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "정상 요청 예시",
                                            value = """
                                                    {
                                                        "studentId": 20011111,
                                                        "password": "asdasda@123"
                                                    }
                                                    """
                                    )
                            }
                    )
            ) @RequestBody @Valid LoginRequest loginRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );


    @Operation(summary = "로그아웃",
            description = "사용자를 로그아웃 처리하고, 저장된 억세스 토큰(쿠키)을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "요청에 포함된 access_token 쿠키가 성공적으로 만료(삭제) 처리된 경우")
    })
    ResponseEntity<Void> logout(@Parameter(hidden = true) HttpServletResponse response);


    @Operation(summary = "포털 로그인 (데모용)",
            description = "세종대학교 포털 인증과 로그인을 동시에 처리합니다. 기존 회원은 로그인 처리되며, 신규 회원은 자동 가입 후 로그인됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포털 인증에 성공하고, 줍줍 서비스 access_token이 쿠키에 설정된 경우",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "포털 로그인으로 즉시 줍줍 로그인 성공 예시",
                                    value = """
                                            {
                                                "memberId": 2,
                                                "message": "로그인에 성공했습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {

                                    @ExampleObject(
                                            name = "필수 요청 필드가 누락된 경우",
                                            value = """
                                                    {
                                                        "title": "유효하지 않은 입력값",
                                                        "status": 400,
                                                        "detail": "portalPassword: 세종대학교 포털 로그인 비밀번호를 입력해 주세요., portalId: 세종대학교 포털 로그인 학번을 입력해 주세요.",
                                                        "instance": "/api/auth/verify-sejong"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "Body에 포함된 JSON 데이터를 파싱에 실패 하거나 JSON 형식이 잘못된 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 요청 본문",
                                                        "status": 400,
                                                        "detail": "요청 본문의 형식이 잘못되었습니다.",
                                                        "instance": "/api/auth/login/portal"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "401", description = "제출된 portalId와 portalPassword가 실제 포털 정보와 일치하지 않아 인증에 실패한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "포털 로그인 실패 예시",
                                    value = """
                                            {
                                                "title": "세종대학교 포털 로그인 실패",
                                                "status": 401,
                                                "detail": "세종대학교 인증에 실패했습니다. 아이디 비밀번호를 다시 한번 확인해 주세요.",
                                                "instance": "/api/auth/login/portal"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "503", description = "세종대학교 포털 서버 자체의 문제나 네트워크 오류로 인해 인증 시도가 불가능한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "포털 인증 실패 예시",
                                    value = """
                                            {
                                                "title": "세종대학교 포털 서버 통신 오류",
                                                "status": 503,
                                                "detail": "세종대학교 포털 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                                                "instance": "/api/auth/login/portal"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LoginResponse> portalLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "세종대학교 포털 로그인 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PortalLoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "정상 요청 예시",
                                            value = """
                                                    {
                                                        "portalId": "2001111 (본인 포탈 로그인 아이디)",
                                                        "portalPassword": "asdasdasd (본인 포탈 로그인 PW)"
                                                    }
                                                    """
                                    )
                            }
                    )
            ) @RequestBody @Valid PortalLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

}
