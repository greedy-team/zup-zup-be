package com.greedy.zupzup.alert.application;

import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.alert.repository.KeywordAlertRepository;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.exception.MemberException;
import com.greedy.zupzup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final KeywordAlertRepository keywordAlertRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

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
