package com.greedy.zupzup.quiz.application.strategy;

import static com.greedy.zupzup.common.fixture.LostItemFeatureFixture.ELECTRONIC_LOST_ITEM_FEATURES;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM;
import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmptyQuizGenerationStrategyTest {

    private final EmptyQuizGenerationStrategy strategy = new EmptyQuizGenerationStrategy();

    @Test
    void 항상_빈리스트를_반환한다() {
        List<QuizData> resultWhenNull = strategy.createQuizzes(null);
        List<QuizData> resultWhenEmpty = strategy.createQuizzes(List.of());

        LostItem lostItem = PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        List<LostItemFeature> features = ELECTRONIC_LOST_ITEM_FEATURES(lostItem);
        List<QuizData> resultWhenFeaturesProvided = strategy.createQuizzes(features);

        assertThat(resultWhenNull).isEmpty();
        assertThat(resultWhenEmpty).isEmpty();
        assertThat(resultWhenFeaturesProvided).isEmpty();
    }
}
