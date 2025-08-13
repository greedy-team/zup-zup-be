package com.greedy.zupzup.quiz.application;

import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.quiz.application.dto.OptionDto;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.exception.QuizException;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final LostItemRepository lostItemRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public List<QuizDto> generateLostItemQuizzes(Long lostItemId, Long memberId) {
        validateQuizGeneration(lostItemId, memberId);

        List<LostItemFeature> lostItemFeatures = lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(
                lostItemId);

        return lostItemFeatures.stream()
                .map(this::createQuizDto)
                .collect(Collectors.toList());
    }

    private void validateQuizGeneration(Long lostItemId, Long memberId) {
        if (!lostItemRepository.existsById(lostItemId)) {
            throw new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND);
        }
        if (quizAttemptRepository.existsByLostItemIdAndMemberId(lostItemId, memberId)) {
            throw new ApplicationException(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED);
        }
    }

    private QuizDto createQuizDto(LostItemFeature lostItemFeature) {
        List<FeatureOption> allOptions = lostItemFeature.getFeature().getOptions();
        FeatureOption correctAnswer = lostItemFeature.getSelectedOption();

        List<FeatureOption> selectedOptions = allOptions.stream()
                .filter(option -> !option.equals(correctAnswer))
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream().limit(3);
                }))
                .collect(Collectors.toList());
        selectedOptions.add(correctAnswer);
        Collections.shuffle(selectedOptions);

        List<OptionDto> optionDtos = selectedOptions.stream()
                .map(option -> new OptionDto(option.getId(), option.getOptionValue()))
                .toList();

        return new QuizDto(lostItemFeature.getFeature().getId(), lostItemFeature.getFeature().getQuizQuestion(),
                optionDtos);
    }
}
