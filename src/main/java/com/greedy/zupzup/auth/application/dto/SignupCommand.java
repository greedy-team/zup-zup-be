package com.greedy.zupzup.auth.application.dto;

public record SignupCommand(
        SejongAuthInfo verifiedInfo,
        Integer studentId,
        String password
) {
}
