package com.greedy.zupzup.member.presentation.dto;

import com.greedy.zupzup.member.application.dto.MemberEmailInfoResult;

public record MemberEmailInfoResponse(
        String email,
        Boolean emailAlertEnabled
) {
    public static MemberEmailInfoResponse from(MemberEmailInfoResult result) {
        return new MemberEmailInfoResponse(result.email(), result.emailAlertEnabled());
    }
}
