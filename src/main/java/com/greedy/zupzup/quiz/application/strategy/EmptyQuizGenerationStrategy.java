package com.greedy.zupzup.quiz.application.strategy;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmptyQuizGenerationStrategy implements QuizGenerationStrategy{

    @Override
    public List<QuizData> createQuizzes(List<LostItemFeature> lostItemFeatures) {
        return Collections.emptyList();
    }
}
