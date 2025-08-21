package com.greedy.zupzup.auth.presentation.dto;

import com.greedy.zupzup.member.domain.Member;

public record LoginResponse(
        Long memberId,
        String message
) {
    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                "로그인에 성공했습니다."
        );
    }
}
