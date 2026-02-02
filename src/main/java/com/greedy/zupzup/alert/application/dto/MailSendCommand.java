package com.greedy.zupzup.alert.application.dto;

import java.time.LocalDate;
import java.util.Map;

public record MailSendCommand(
        String to,
        String subject,
        Map<String, CategoryDigest> categoryDigests,
        long totalCount,
        String date,
        String baseUrl
) {
    public static MailSendCommand of(
            String email,
            Map<String, CategoryDigest> categoryDigests,
            long totalCount,
            String baseUrl
    ) {
        return new MailSendCommand(
                email,
                "[줍줍] " + LocalDate.now() + " 새 분실물 알림",
                categoryDigests,
                totalCount,
                LocalDate.now().toString(),
                baseUrl
        );
    }
}
