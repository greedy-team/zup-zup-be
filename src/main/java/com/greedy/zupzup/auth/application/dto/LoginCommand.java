package com.greedy.zupzup.auth.application.dto;

public record LoginCommand(
        Integer studentId,
        String password
) {
}
