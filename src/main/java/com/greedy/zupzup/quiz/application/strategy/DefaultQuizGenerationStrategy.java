package com.greedy.zupzup.quiz.application.strategy;

import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.OptionData;
import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultQuizGenerationStrategy implements QuizGenerationStrategy {

    private static final int QUIZ_OPTIONS_COUNT = 4;
    private static final int CORRECT_ANSWER_COUNT = 1;
    private static final int NUMBER_OF_WRONG_OPTIONS = QUIZ_OPTIONS_COUNT - CORRECT_ANSWER_COUNT;
    private static final String ETC_OPTION_TEXT = "기타";

    @Override
    public List<QuizData> createQuizzes(List<LostItemFeature> lostItemFeatures) {
        if (lostItemFeatures == null || lostItemFeatures.isEmpty()) {
            return Collections.emptyList();
        }
        return lostItemFeatures.stream()
                .map(this::createQuizDto)
                .collect(Collectors.toList());
    }

    private QuizData createQuizDto(LostItemFeature lostItemFeature) {
        List<FeatureOption> allOptions = lostItemFeature.getFeatureOptions();
        FeatureOption correctAnswer = lostItemFeature.getSelectedOption();

        List<FeatureOption> selectedOptions = selectQuizOptions(allOptions, correctAnswer);
        sortQuizOptions(selectedOptions);

        List<OptionData> optionData = selectedOptions.stream()
                .map(OptionData::from)
                .toList();

        return QuizData.of(lostItemFeature, optionData);
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
