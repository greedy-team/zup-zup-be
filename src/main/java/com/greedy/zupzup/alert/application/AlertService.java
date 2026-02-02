package com.greedy.zupzup.alert.application;

import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.alert.repository.KeywordAlertRepository;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.member.domain.Member;
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

        keywordAlertRepository.deleteAllByMemberId(member.getId());

        if (command.categoryIds() == null || command.categoryIds().isEmpty()) {
            return;
        }

        List<Long> uniqueCategoryIds = command.categoryIds().stream()
                .distinct()
                .toList();

        List<Category> categories = categoryRepository.getAllByIds(uniqueCategoryIds);

        List<KeywordAlert> newAlerts = categories.stream()
                .map(category -> KeywordAlert.subscribe(member, category))
                .toList();

        keywordAlertRepository.saveAll(newAlerts);
    }
}
