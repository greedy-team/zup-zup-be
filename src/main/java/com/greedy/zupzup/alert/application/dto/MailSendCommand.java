package com.greedy.zupzup.alert.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record MailSendCommand(
        String to,
        String subject,
        Map<String, CategoryDigest> categoryDigests,
        long totalCount,
        String date,
        String baseUrl,
        String templateName
) {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static MailSendCommand ofApproval(
            String email,
            Map<String, CategoryDigest> categoryDigests,
            LocalDateTime registeredAt,
            String baseUrl
    ) {
        return new MailSendCommand(
                email,
                "[줍줍] 관심 분실물이 등록되었습니다 📬",
                categoryDigests,
                0,
                registeredAt.format(DATETIME_FORMATTER),
                baseUrl,
                "alert-approval"
        );
    }

    public static MailSendCommand ofDailyDigest(
            String email,
            Map<String, CategoryDigest> categoryDigests,
            long totalCount,
            String baseUrl
    ) {
        return new MailSendCommand(
                email,
                "[줍줍] " + LocalDate.now() + " 오늘의 분실물 소식입니다 📬",
                categoryDigests,
                totalCount,
                LocalDate.now().toString(),
                baseUrl,
                "alert-digest"
        );
    }
}
