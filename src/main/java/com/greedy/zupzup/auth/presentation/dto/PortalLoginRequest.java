package com.greedy.zupzup.auth.presentation.dto;

import com.greedy.zupzup.auth.application.dto.PortalLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record PortalLoginRequest(

        @NotBlank(message = "세종대학교 포털 로그인 학번을 입력해 주세요.")
        String portalId,

        @NotBlank(message = "세종대학교 포털 로그인 비밀번호를 입력해 주세요.")
        String portalPassword
) {
    public PortalLoginCommand toCommand() {
        return new PortalLoginCommand(this.portalId, this.portalPassword);
    }
}
