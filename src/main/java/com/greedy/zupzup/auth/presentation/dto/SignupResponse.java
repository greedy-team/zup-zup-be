package com.greedy.zupzup.auth.presentation.dto;

import com.greedy.zupzup.member.domain.Member;

public record SignupResponse(
        Long memberId,
        String message
) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(
                member.getId(),
                "회원가입에 성공했습니다!"
        );
    }
}
