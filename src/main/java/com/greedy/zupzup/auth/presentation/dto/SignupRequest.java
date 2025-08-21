package com.greedy.zupzup.auth.presentation.dto;

import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.application.dto.SignupCommand;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record SignupRequest(

        @NotNull(message = "학번은 필수입니다.")
        int studentId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z\\d~!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]{6,20}$",
                message = "비밀번호는 6~20자 길이의 영문, 숫자, 특수문자만 사용할 수 있습니다."
        )
        String password
) {

    public SignupCommand toCommand(SejongAuthInfo sejongAuthInfo) {
        return new SignupCommand(
                sejongAuthInfo,
                this.studentId,
                this.password
        );
    }
}
