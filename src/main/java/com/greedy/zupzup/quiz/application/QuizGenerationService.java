package com.greedy.zupzup.quiz.application;

import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
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
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final LostItemRepository lostItemRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    private static final int QUIZ_OPTIONS_COUNT = 4;
    private static final int CORRECT_ANSWER_COUNT = 1;
    private static final int NUMBER_OF_WRONG_OPTIONS = QUIZ_OPTIONS_COUNT - CORRECT_ANSWER_COUNT;
    private static final String ETC_CATEGORY_NAME = "기타";
    private static final String ETC_OPTION_TEXT = "기타";

    @Transactional(readOnly = true)
    public List<QuizDto> generateLostItemQuizzes(Long lostItemId, Long memberId) {
        validateQuizGeneration(lostItemId, memberId);

        LostItem lostItem = lostItemRepository.findWithCategoryById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));

        if (lostItem.isNotQuizCategory()) {
            return Collections.emptyList();
        }

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

        List<FeatureOption> selectedOptions = selectQuizOptions(allOptions, correctAnswer);

        sortQuizOptions(selectedOptions);

        List<OptionDto> optionDtos = selectedOptions.stream()
                .map(OptionDto::from)
                .toList();

        return QuizDto.of(lostItemFeature, optionDtos);
    }

    private List<FeatureOption> selectQuizOptions(List<FeatureOption> allOptions, FeatureOption correctAnswer) {
        List<FeatureOption> wrongOptions = allOptions.stream()
                .filter(option -> !option.equals(correctAnswer))
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream().limit(NUMBER_OF_WRONG_OPTIONS);
                }))
                .collect(Collectors.toList());

        wrongOptions.add(correctAnswer);
        return wrongOptions;
    }

    private void sortQuizOptions(List<FeatureOption> options) {
        Optional<FeatureOption> etcOption = options.stream()
                .filter(option -> ETC_OPTION_TEXT.equals(option.getOptionValue()))
                .findFirst();

        if (etcOption.isPresent()) {
            options.remove(etcOption.get());
            Collections.shuffle(options);
            options.add(etcOption.get());
        } else {
            Collections.shuffle(options);
        }
    }
}
