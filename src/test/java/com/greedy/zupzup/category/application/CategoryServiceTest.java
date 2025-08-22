package com.greedy.zupzup.category.application;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.ETC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.WALLET;
import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_BRAND;
import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_COLOR;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_BRAND_APPLE;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_BRAND_SAMSUNG;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_COLOR_BLACK;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_COLOR_SILVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.category.application.dto.FeatureWithOptionsDto;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest extends ServiceUnitTest {

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("카테고리 전체 조회 API")
    class GetAll {

        @Test
        void 카테고리_전체_조회에_성공하면_CategoriesResponse와_정렬된_목록을_반환한다() {
            Category elec = ELECTRONIC();
            setId(elec, 1L);
            Category wallet = WALLET();
            setId(wallet, 2L);
            Category etc = ETC();
            setId(etc, 3L);

            given(categoryRepository.findAllByOrderByIdAsc())
                    .willReturn(List.of(elec, wallet, etc));

            // when
            CategoriesResponse response = categoryService.getAll();

            // then
            assertSoftly(s -> {
                s.assertThat(response.categories()).hasSize(3);
                s.assertThat(response.categories().get(0).name()).isEqualTo(elec.getName());
                s.assertThat(response.categories().get(1).name()).isEqualTo(wallet.getName());
                s.assertThat(response.categories().get(2).name()).isEqualTo(etc.getName());
            });
            then(categoryRepository).should().findAllByOrderByIdAsc();
        }

        @Test
        void 카테고리가_없으면_빈_리스트를_반환한다() {
            // given
            given(categoryRepository.findAllByOrderByIdAsc())
                    .willReturn(List.of());

            // when
            CategoriesResponse response = categoryService.getAll();

            // then
            assertThat(response.categories()).isEmpty();
            then(categoryRepository).should().findAllByOrderByIdAsc();
        }
    }

    @Nested
    @DisplayName("카테고리 별 전체 특징/옵션 조회 API")
    class GetCategoryFeaturesAndOptions {
        @Test
        void 카테고리별_특징과_옵션을_조회해_옵션이_묶여서_반환된다() {
            // given
            Category electronic = ELECTRONIC();
            setId(electronic, 10L);

            Feature brand = ELECTRONIC_BRAND(electronic);
            setId(brand, 101L);
            Feature color = ELECTRONIC_COLOR(electronic);
            setId(color, 102L);

            ReflectionTestUtils.setField(electronic, "features", new ArrayList<>(List.of(brand, color)));

            given(categoryRepository.findWithFeaturesById(10L))
                    .willReturn(Optional.of(electronic));

            FeatureOption samsung = ELECTRONIC_BRAND_SAMSUNG(brand);
            setId(samsung, 1001L);
            FeatureOption apple = ELECTRONIC_BRAND_APPLE(brand);
            setId(apple, 1002L);

            FeatureOption black = ELECTRONIC_COLOR_BLACK(color);
            setId(black, 2001L);
            FeatureOption silver = ELECTRONIC_COLOR_SILVER(color);
            setId(silver, 2002L);

            given(featureOptionRepository.findByFeatureIds(List.of(101L, 102L)))
                    .willReturn(List.of(samsung, apple, black, silver));

            // when
            CategoryFeaturesResponse response = categoryService.getCategoryFeatures(10L);

            // then
            assertSoftly(s -> {
                s.assertThat(response.categoryId()).isEqualTo(10L);
                s.assertThat(response.categoryName()).isEqualTo("전자기기");
                s.assertThat(response.features()).hasSize(2);

                s.assertThat(response.features())
                        .extracting(FeatureWithOptionsDto::name, FeatureWithOptionsDto::id, FeatureWithOptionsDto::quizQuestion)
                        .containsExactlyInAnyOrder(
                                tuple("브랜드", 101L, brand.getQuizQuestion()),
                                tuple("색상", 102L, color.getQuizQuestion())
                        );

                FeatureWithOptionsDto brandDto = featureByName(response, "브랜드");
                s.assertThat(brandDto.options())
                        .extracting(FeatureOptionDto::optionValue)
                        .containsExactlyInAnyOrder("삼성", "애플");

                FeatureWithOptionsDto colorDto = featureByName(response, "색상");
                s.assertThat(colorDto.options())
                        .extracting(FeatureOptionDto::optionValue)
                        .containsExactlyInAnyOrder("블랙", "실버");
            });

            then(categoryRepository).should().findWithFeaturesById(10L);
            then(featureOptionRepository).should().findByFeatureIds(List.of(101L, 102L));
        }

        @Test
        void 특징이_없는_카테고리는_옵션_조회없이_빈_특징_리스트를_반환한다() {
            // given
            Category etc = ETC();
            setId(etc, 77L);
            ReflectionTestUtils.setField(etc, "features", List.of());

            given(categoryRepository.findWithFeaturesById(77L))
                    .willReturn(Optional.of(etc));

            // when
            CategoryFeaturesResponse response = categoryService.getCategoryFeatures(77L);

            // then
            assertSoftly(s -> {
                s.assertThat(response.categoryId()).isEqualTo(77L);
                s.assertThat(response.features()).isEmpty();
            });

            then(categoryRepository).should().findWithFeaturesById(77L);
            verifyNoInteractions(featureOptionRepository);
        }

        @Test
        void 존재하지_않는_카테고리면_예외가_발생한다() {
            // given
            given(categoryRepository.findWithFeaturesById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.getCategoryFeatures(999L))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CategoryException.CATEGORY_NOT_FOUND.getDetail());

            then(categoryRepository).should().findWithFeaturesById(999L);
            verifyNoInteractions(featureOptionRepository);
        }
    }

    private static FeatureWithOptionsDto featureByName(
            CategoryFeaturesResponse resp, String name) {
        return resp.features().stream()
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
