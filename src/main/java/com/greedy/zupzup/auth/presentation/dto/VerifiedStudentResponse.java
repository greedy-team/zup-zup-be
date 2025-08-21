package com.greedy.zupzup.auth.presentation.dto;

public record VerifiedStudentResponse(
        int studentId,
        String message
) {
    public static VerifiedStudentResponse from(int studentId) {
        return new VerifiedStudentResponse(studentId, "세종대학교 학생 인증에 성공했습니다.");
    }
}
