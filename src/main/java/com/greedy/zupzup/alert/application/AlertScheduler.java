package com.greedy.zupzup.alert.application;

import com.greedy.zupzup.alert.application.dto.CategoryDigest;
import com.greedy.zupzup.alert.application.dto.MailSendCommand;
import com.greedy.zupzup.alert.repository.AlertDigestProjection;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private static final String DEFAULT_CATEGORY_EMOJI = "📦";
    private static final String BASE_URL = "https://www.sejong-zupzup.kr";

    private final LostItemRepository lostItemRepository;
    private final EmailSender emailSender;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyDigest() {
        log.info("[AlertScheduler] 일일 다이제스트 발송 시작");

        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
        LocalDateTime start = end.minusDays(1);

        List<AlertDigestProjection> digestData =
                lostItemRepository.findDigestData(start, end);

        if (digestData.isEmpty()) {
            log.info("[AlertScheduler] 발송할 알림 데이터가 없습니다.");
            return;
        }

        Map<String, List<AlertDigestProjection>> mailDataMap =
                digestData.stream()
                        .collect(Collectors.groupingBy(AlertDigestProjection::getEmail));

        mailDataMap.forEach(this::sendMail);

        log.info(
                "[AlertScheduler] 일일 다이제스트 발송 완료 (총 대상자: {}명)",
                mailDataMap.size()
        );
    }

    private void sendMail(String email, List<AlertDigestProjection> projections) {
        Map<String, CategoryDigest> categoryDigests =
                createCategoryDigests(projections);

        long totalCount = projections.stream()
                .mapToLong(AlertDigestProjection::getCount)
                .sum();

        MailSendCommand command = createMailCommand(
                email,
                categoryDigests,
                totalCount
        );

        emailSender.sendEmail(command);
    }

    private Map<String, CategoryDigest> createCategoryDigests(
            List<AlertDigestProjection> projections
    ) {
        Map<String, List<AlertDigestProjection>> groupedByCategory =
                projections.stream()
                        .collect(Collectors.groupingBy(
                                AlertDigestProjection::getCategoryName
                        ));

        Map<String, CategoryDigest> result = new HashMap<>();

        groupedByCategory.forEach((categoryName, items) -> {
            long totalCount = items.stream()
                    .mapToLong(AlertDigestProjection::getCount)
                    .sum();

            Map<String, Long> itemsByLocation =
                    items.stream()
                            .collect(Collectors.toMap(
                                    AlertDigestProjection::getAreaName,
                                    AlertDigestProjection::getCount,
                                    Long::sum
                            ));

            String emoji = items.get(0).getCategoryEmoji();
            if (emoji == null || emoji.isBlank()) {
                emoji = DEFAULT_CATEGORY_EMOJI;
            }

            result.put(
                    categoryName,
                    CategoryDigest.of(
                            categoryName,
                            emoji,
                            totalCount,
                            itemsByLocation
                    )
            );
        });

        return result;
    }

    private MailSendCommand createMailCommand(
            String email,
            Map<String, CategoryDigest> categoryDigests,
            long totalCount
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("categoryDigests", categoryDigests);
        variables.put("totalCount", totalCount);
        variables.put("date", LocalDate.now().toString());
        variables.put("baseUrl", BASE_URL);

        return MailSendCommand.of(
                email,
                "[줍줍] " + LocalDate.now() + " 새 분실물 알림",
                variables
        );
    }
}
