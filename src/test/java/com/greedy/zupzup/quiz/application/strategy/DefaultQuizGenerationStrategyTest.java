package com.greedy.zupzup.quiz.application.strategy;

import static com.greedy.zupzup.common.fixture.LostItemFeatureFixture.ELECTRONIC_LOST_ITEM_FEATURES;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM;
import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.application.dto.OptionDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultQuizGenerationStrategyTest {

    private static final int QUIZ_OPTIONS_COUNT = 4;
    private static final String ETC_OPTION_TEXT = "기타";

    private DefaultQuizGenerationStrategy strategy;

    private List<LostItemFeature> features;

    @BeforeEach
    void setUp() {
        strategy = new DefaultQuizGenerationStrategy();
        LostItem lostItem = PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        features = ELECTRONIC_LOST_ITEM_FEATURES(lostItem);
    }

    @Test
    void 입력이_null_혹은_빈_리스트면_빈_결과를_반환한다() {
        assertThat(strategy.createQuizzes(null)).isEmpty();
        assertThat(strategy.createQuizzes(List.of())).isEmpty();
    }

    @Test
    void 특징별로_4지선다를_생성하고_정답을_포함하며_중복_없이_구성한다() {
        // when
        List<QuizDto> quizzes = strategy.createQuizzes(features);

        // then
        assertThat(quizzes).hasSize(features.size());
        for (QuizDto quiz : quizzes) {
            assertThat(quiz.options()).hasSize(QUIZ_OPTIONS_COUNT);

            List<String> texts = quiz.options().stream().map(OptionDto::text).toList();
            assertThat(Set.copyOf(texts)).hasSize(QUIZ_OPTIONS_COUNT); // 중복 테스트

            boolean containsCorrect =
                    quiz.question().contains("브랜드") ? texts.contains("삼성")
                            : quiz.question().contains("색상") && texts.contains("블랙");
            assertThat(containsCorrect).isTrue();
        }
    }

    @Test
    void 옵션에_기타가_있다면_항상_마지막_인덱스에_위치한다() {
        // when
        List<QuizDto> quizzes = strategy.createQuizzes(features);

        // then
        for (QuizDto quiz : quizzes) {
            List<String> texts = quiz.options().stream().map(OptionDto::text).toList();
            assertThat(texts).hasSize(QUIZ_OPTIONS_COUNT);
            if (texts.contains(ETC_OPTION_TEXT)) {
                assertThat(texts.get(QUIZ_OPTIONS_COUNT - 1)).isEqualTo(ETC_OPTION_TEXT);
            }
        }
    }

    @Test
    void 브랜드와_색상_각_퀴즈가_정상적으로_생성된다() {
        // when
        List<QuizDto> quizzes = strategy.createQuizzes(features);

        // then
        QuizDto brandQuiz = quizzes.stream()
                .filter(q -> q.question().contains("브랜드"))
                .findFirst().orElseThrow();
        QuizDto colorQuiz = quizzes.stream()
                .filter(q -> q.question().contains("색상"))
                .findFirst().orElseThrow();

        assertThat(brandQuiz.options()).hasSize(QUIZ_OPTIONS_COUNT);
        assertThat(colorQuiz.options()).hasSize(QUIZ_OPTIONS_COUNT);

        List<String> brandOptions = brandQuiz.options().stream().map(OptionDto::text).collect(Collectors.toList());
        List<String> colorOptions = colorQuiz.options().stream().map(OptionDto::text).collect(Collectors.toList());

        assertThat(brandOptions).contains("삼성");
        assertThat(colorOptions).contains("블랙");

        assertThat(brandOptions.get(QUIZ_OPTIONS_COUNT - 1)).isEqualTo(ETC_OPTION_TEXT);
        if (colorOptions.contains(ETC_OPTION_TEXT)) {
            assertThat(colorOptions.get(QUIZ_OPTIONS_COUNT - 1)).isEqualTo("기타");
        }
    }
}
