package com.greedy.zupzup.auth.presentation.dto;

public record VerifiedStudentResponse(
        Integer studentId,
        String message
) {
    public static VerifiedStudentResponse from(Integer studentId) {
        return new VerifiedStudentResponse(studentId, "세종대학교 인증에 성공했습니다.");
    }
}
