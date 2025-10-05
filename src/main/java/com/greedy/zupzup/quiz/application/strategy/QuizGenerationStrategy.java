package com.greedy.zupzup.quiz.application.strategy;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import java.util.List;

public interface QuizGenerationStrategy {

    List<QuizDto> createQuizzes(List<LostItemFeature> lostItemFeatures);
}
