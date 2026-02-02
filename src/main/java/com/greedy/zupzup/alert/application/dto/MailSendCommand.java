package com.greedy.zupzup.alert.application.dto;

import java.util.Map;

public record MailSendCommand(
        String to,
        String subject,
        Map<String, Object> variables
) {
    public static MailSendCommand of(String to, String subject, Map<String, Object> variables) {
        return new MailSendCommand(to, subject, variables);
    }
}
