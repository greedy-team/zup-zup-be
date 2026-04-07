package com.greedy.zupzup.alert.application;

import com.greedy.zupzup.alert.application.dto.CategoryDigest;
import com.greedy.zupzup.alert.application.dto.MailSendCommand;
import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.alert.repository.AlertDigestProjection;
import com.greedy.zupzup.alert.repository.KeywordAlertRepository;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.exception.MemberException;
import com.greedy.zupzup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final String DEFAULT_CATEGORY_EMOJI = "📦";
    private static final String BASE_URL = "https://www.sejong-zupzup.kr";

    private final KeywordAlertRepository keywordAlertRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final LostItemRepository lostItemRepository;
    private final EmailSender emailSender;

    @Transactional(readOnly = true)
    public SubscriptionResult getSubscriptions(Long memberId) {
        List<Long> categoryIds = keywordAlertRepository.findAllByMemberId(memberId)
                .stream()
                .map(alert -> alert.getCategory().getId())
                .toList();

        return SubscriptionResult.of(categoryIds);
    }

    @Transactional
    public void updateSubscriptions(SubscriptionUpdateCommand command) {
        Member member = memberRepository.getById(command.memberId());

        if (!member.hasEmail()) {
            throw new ApplicationException(MemberException.EMAIL_NOT_REGISTERED);
        }

        List<Long> requestCategoryIds = getUniqueCategoryIds(command.categoryIds());
        List<KeywordAlert> existingAlerts = keywordAlertRepository.findAllByMemberId(member.getId());

        deleteUnsubscribedAlerts(existingAlerts, requestCategoryIds);
        addNewSubscriptions(member, existingAlerts, requestCategoryIds);
    }

    @Transactional(readOnly = true)
    public void sendApprovalAlert(Long approvedId) {
        LostItem lostItem = lostItemRepository.getById(approvedId);
        LocalDateTime registeredAt = lostItem.getCreatedAt();

        List<AlertDigestProjection> digestData = lostItemRepository.findDigestDataByIds(List.of(approvedId));

        if (digestData.isEmpty()) {
            log.info("[AlertService] 알림 대상자가 없습니다. (id: {})", approvedId);
            return;
        }

        Map<String, List<AlertDigestProjection>> mailDataMap = digestData.stream()
                .collect(Collectors.groupingBy(AlertDigestProjection::getEmail));

        mailDataMap.forEach((email, projections) -> sendApprovalMail(email, projections, registeredAt));

        log.info("[AlertService] 실시간 알림 발송 완료 (총 대상자: {}명)", mailDataMap.size());
    }

    @Transactional(readOnly = true)
    public void sendDailyDigest(List<Long> ids) {
        List<AlertDigestProjection> digestData = lostItemRepository.findDigestDataByIds(ids);

        if (digestData.isEmpty()) {
            log.info("[AlertService] 알림 대상자가 없습니다.");
            return;
        }

        Map<String, List<AlertDigestProjection>> mailDataMap = digestData.stream()
                .collect(Collectors.groupingBy(AlertDigestProjection::getEmail));

        mailDataMap.forEach(this::sendDailyMail);

        log.info("[AlertService] 일일 알림 발송 완료 (총 대상자: {}명)", mailDataMap.size());
    }

    private void sendApprovalMail(
            String email,
            List<AlertDigestProjection> projections,
            LocalDateTime registeredAt
    ) {
        Map<String, CategoryDigest> categoryDigests = createCategoryDigests(projections);

        MailSendCommand command = MailSendCommand.ofApproval(email, categoryDigests, registeredAt, BASE_URL);
        emailSender.sendEmail(command);
    }

    private void sendDailyMail(String email, List<AlertDigestProjection> projections) {
        Map<String, CategoryDigest> categoryDigests = createCategoryDigests(projections);
        long totalCount = projections.stream().mapToLong(AlertDigestProjection::getCount).sum();

        MailSendCommand command = MailSendCommand.ofDailyDigest(email, categoryDigests, totalCount, BASE_URL);
        emailSender.sendEmail(command);
    }

    private Map<String, CategoryDigest> createCategoryDigests(List<AlertDigestProjection> projections) {
        Map<String, List<AlertDigestProjection>> groupedByCategory = projections.stream()
                .collect(Collectors.groupingBy(AlertDigestProjection::getCategoryName));

        Map<String, CategoryDigest> result = new HashMap<>();

        groupedByCategory.forEach((categoryName, items) -> {
            long totalCount = items.stream().mapToLong(AlertDigestProjection::getCount).sum();
            Map<String, Long> itemsByLocation = items.stream()
                    .collect(Collectors.toMap(
                            AlertDigestProjection::getAreaName,
                            AlertDigestProjection::getCount,
                            Long::sum
                    ));

            String emoji = items.get(0).getCategoryEmoji();
            if (emoji == null || emoji.isBlank()) emoji = DEFAULT_CATEGORY_EMOJI;

            result.put(categoryName, CategoryDigest.of(categoryName, emoji, totalCount, itemsByLocation));
        });

        return result;
    }

    private List<Long> getUniqueCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null) {
            return List.of();
        }
        return categoryIds.stream().distinct().toList();
    }

    private void deleteUnsubscribedAlerts(List<KeywordAlert> existingAlerts, List<Long> requestCategoryIds) {
        List<KeywordAlert> alertsToDelete = existingAlerts.stream()
                .filter(alert -> !requestCategoryIds.contains(alert.getCategory().getId()))
                .toList();

        if (!alertsToDelete.isEmpty()) {
            keywordAlertRepository.deleteAll(alertsToDelete);
        }
    }

    private void addNewSubscriptions(Member member, List<KeywordAlert> existingAlerts, List<Long> requestCategoryIds) {
        List<Long> existingCategoryIds = existingAlerts.stream()
                .map(alert -> alert.getCategory().getId())
                .toList();

        List<Long> categoryIdsToAdd = requestCategoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        if (!categoryIdsToAdd.isEmpty()) {
            List<Category> categories = categoryRepository.getAllByIds(categoryIdsToAdd);
            List<KeywordAlert> newAlerts = categories.stream()
                    .map(category -> KeywordAlert.subscribe(member, category))
                    .toList();
            keywordAlertRepository.saveAll(newAlerts);
        }
    }
}