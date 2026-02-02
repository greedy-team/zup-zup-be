package com.greedy.zupzup.alert.application;

import com.greedy.zupzup.alert.application.dto.MailSendCommand;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {

    private static final String TEMPLATE_NAME = "alert-digest";
    private static final String ENCODING = "UTF-8";
    private static final String SENDER_NAME = "줍줍(ZupZup)";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("mailExecutor")
    public void sendEmail(MailSendCommand command) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);

            Context context = new Context();
            context.setVariable("categoryDigests", command.categoryDigests());
            context.setVariable("totalCount", command.totalCount());
            context.setVariable("baseUrl", command.baseUrl());
            context.setVariable("date", command.date());

            String htmlContent = templateEngine.process(TEMPLATE_NAME, context);

            helper.setTo(command.to());
            helper.setSubject(command.subject());
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail, SENDER_NAME);

            mailSender.send(message);
            log.info("[EmailSender] 메일 전송 성공! (Target: {})", command.to());

        } catch (Exception e) {
            log.error("[EmailSender] 메일 전송 실패 (Target: {})", command.to(), e);
        }
    }
}
