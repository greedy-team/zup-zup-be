package com.greedy.zupzup.auth.presentation.dto;

import com.greedy.zupzup.auth.application.dto.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank(message = "줍줍 로그인 학번을 입력해 주세요.")
        @Pattern(regexp = "[0-9]+", message = "학번은 숫자만 입력 가능합니다.")
        String studentId,

        @NotBlank(message = "줍줍 로그인 비밀번호를 입력해 주세요.")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(
                Integer.parseInt(studentId),
                this.password
        );
    }
}
