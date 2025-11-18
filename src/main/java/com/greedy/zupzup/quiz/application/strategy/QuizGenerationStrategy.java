package com.greedy.zupzup.quiz.application.strategy;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.List;

public interface QuizGenerationStrategy {

    List<QuizData> createQuizzes(List<LostItemFeature> lostItemFeatures);
}
