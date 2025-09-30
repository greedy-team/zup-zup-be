package com.greedy.zupzup.common;

import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.application.strategy.DefaultQuizGenerationStrategy;
import com.greedy.zupzup.quiz.application.strategy.EmptyQuizGenerationStrategy;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceUnitTest {

    @Mock
    protected LostItemRepository lostItemRepository;

    @Mock
    protected LostItemImageRepository lostItemImageRepository;

    @Mock
    protected LostItemFeatureRepository lostItemFeatureRepository;

    @Mock
    protected FeatureOptionRepository featureOptionRepository;

    @Mock
    protected SchoolAreaRepository schoolAreaRepository;

    @Mock
    protected CategoryRepository categoryRepository;

    @Mock
    protected S3ImageFileManager s3ImageFileManager;

    @Mock
    protected MemberRepository memberRepository;

    @Mock
    protected QuizAttemptRepository quizAttemptRepository;

    @Mock
    protected PledgeRepository pledgeRepository;

    @Mock
    protected DefaultQuizGenerationStrategy defaultQuizGenerationStrategy;

    @Mock
    protected EmptyQuizGenerationStrategy emptyQuizGenerationStrategy;

    protected static void setId(Object target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
    }
}
