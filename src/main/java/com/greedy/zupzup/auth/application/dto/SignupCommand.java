package com.greedy.zupzup.auth.application.dto;

public record SignupCommand(
        SejongAuthInfo verifiedInfo,
        int studentId,
        String password
) {
}
