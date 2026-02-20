package com.greedy.zupzup.member.application.dto;

public record MemberEmailUpdateCommand(
        Long memberId,
        String email,
        Boolean emailAlertEnabled
) {
    public static MemberEmailUpdateCommand of(Long memberId, String email, Boolean emailAlertEnabled) {
        return new MemberEmailUpdateCommand(memberId, email, emailAlertEnabled);
    }
}
