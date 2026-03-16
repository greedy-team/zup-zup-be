package com.greedy.zupzup.member.application.dto;

import com.greedy.zupzup.member.domain.Member;

public record MemberEmailInfoResult(
        String email,
        Boolean emailAlertEnabled
) {
    public static MemberEmailInfoResult from(Member member) {
        return new MemberEmailInfoResult(member.getEmail(), member.getEmailAlertEnabled());
    }
}