package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.common.fixture.CategoryFixture;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.ItemFeatureOptionCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.application.dto.UploadedImageData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_COLOR;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_COLOR_OPTIONS;
import static com.greedy.zupzup.common.fixture.SchoolAreaFixture.AI_CENTER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

class LostItemStorageServiceTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemStorageService lostItemStorageService;

    private static final Long VALID_CATEGORY_ID = 1L;
    private static final Long VALID_ETC_CATEGORY_ID = 2L;
    private static final Long VALID_FEATURE_ID = 2L;
    private static final Long VALID_OPTION_ID = 3L;
    private static final Long VALID_FOUND_AREA_ID = 4L;

    private Category mockCategory;
    private Category mockETCCategory;
    private Feature mockFeature;
    private List<FeatureOption> mockOptions;
    private SchoolArea mockSchoolArea;

    @BeforeEach
    void setUp() {
        mockCategory = ELECTRONIC();
        mockETCCategory = CategoryFixture.ETC();
        mockFeature = ELECTRONIC_COLOR(mockCategory);
        mockOptions = ELECTRONIC_COLOR_OPTIONS(mockFeature);
        mockSchoolArea = AI_CENTER();

        ReflectionTestUtils.setField(mockCategory, "id", VALID_CATEGORY_ID);
        ReflectionTestUtils.setField(mockETCCategory, "id", VALID_ETC_CATEGORY_ID);
        ReflectionTestUtils.setField(mockFeature, "id", VALID_FEATURE_ID);
        ReflectionTestUtils.setField(mockOptions.get(0), "id", VALID_OPTION_ID);
        ReflectionTestUtils.setField(mockSchoolArea, "id", VALID_FOUND_AREA_ID);
        ReflectionTestUtils.setField(mockCategory, "features", List.of(mockFeature));
    }

    private CreateLostItemCommand createDummyCommand(Long categoryId, Long featureId, Long optionId) {
        return new CreateLostItemCommand(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                VALID_FOUND_AREA_ID,
                "AI 센터 B205",
                categoryId,
                List.of(new ItemFeatureOptionCommand(featureId, optionId)),
                List.of()
        );
    }

    private CreateLostItemCommand createETCDummyCommand(Long categoryId) {
        return new CreateLostItemCommand(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                VALID_FOUND_AREA_ID,
                "AI 센터 B205",
                categoryId,
                List.of(),
                List.of()
        );
    }

    @Nested
    @DisplayName("분실물 생성 유효성 검사")
    class GetValidRegisterData {

        @Test
        void 유효성_검사에_성공하면_검증된_데이터를_반환해야_한다() {
            // given
            CreateLostItemCommand command = createDummyCommand(VALID_CATEGORY_ID, VALID_FEATURE_ID, VALID_OPTION_ID);

            given(categoryRepository.findWithFeaturesById(command.categoryId())).willReturn(Optional.of(mockCategory));
            given(schoolAreaRepository.getAreaById(command.foundAreaId())).willReturn(mockSchoolArea);
            given(featureOptionRepository.findByFeatureIds(anyList())).willReturn(mockOptions);

            // when
            LostItemRegisterData result = lostItemStorageService.getValidRegisterData(command);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.category()).isEqualTo(mockCategory);
                softly.assertThat(result.foundSchoolArea()).isEqualTo(mockSchoolArea);
                softly.assertThat(result.itemFeatureAndOptions()).hasSize(1);
                softly.assertThat(result.isNonETC()).isTrue();
            });
        }

        @Test
        void 기타_카테고리는_특징값_검증_없이_성공해야_한다() {
            // given
            CreateLostItemCommand command = createETCDummyCommand(VALID_ETC_CATEGORY_ID);

            given(categoryRepository.findWithFeaturesById(command.categoryId())).willReturn(Optional.of(mockETCCategory));
            given(schoolAreaRepository.getAreaById(command.foundAreaId())).willReturn(mockSchoolArea);

            // when
            LostItemRegisterData result = lostItemStorageService.getValidRegisterData(command);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.category()).isEqualTo(mockETCCategory);
                softly.assertThat(result.foundSchoolArea()).isEqualTo(mockSchoolArea);
                softly.assertThat(result.itemFeatureAndOptions()).isEmpty();
                softly.assertThat(result.isNonETC()).isFalse();
            });
            then(featureOptionRepository).should(never()).findByFeatureIds(anyList());
        }

        @Test
        void 존재하지_않는_카테고리면_예외가_발생해야_한다() {
            // given
            Long invalidCategoryId = 99L;
            CreateLostItemCommand command = createDummyCommand(invalidCategoryId, VALID_FEATURE_ID, VALID_OPTION_ID);
            given(categoryRepository.findWithFeaturesById(invalidCategoryId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lostItemStorageService.getValidRegisterData(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CategoryException.CATEGORY_NOT_FOUND.getDetail());
        }

        @Test
        void 카테고리와_일치하지_않는_특징이면_예외가_발생해야_한다() {
            // given
            Long invalidFeatureId = 99L;
            CreateLostItemCommand command = createDummyCommand(VALID_CATEGORY_ID, invalidFeatureId, VALID_OPTION_ID);
            given(categoryRepository.findWithFeaturesById(VALID_CATEGORY_ID)).willReturn(Optional.of(mockCategory));
            given(schoolAreaRepository.getAreaById(command.foundAreaId())).willReturn(mockSchoolArea);
            given(featureOptionRepository.findByFeatureIds(anyList())).willReturn(mockOptions);

            // when & then
            assertThatThrownBy(() -> lostItemStorageService.getValidRegisterData(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CategoryException.INVALID_CATEGORY_FEATURE.getDetail());
        }

        @Test
        void 특징에_속하지_않는_옵션이면_예외가_발생해야_한다() {
            // given
            Long invalidOptionId = 99L;
            CreateLostItemCommand command = createDummyCommand(VALID_CATEGORY_ID, VALID_FEATURE_ID, invalidOptionId);
            given(categoryRepository.findWithFeaturesById(VALID_CATEGORY_ID)).willReturn(Optional.of(mockCategory));
            given(schoolAreaRepository.getAreaById(command.foundAreaId())).willReturn(mockSchoolArea);
            given(featureOptionRepository.findByFeatureIds(anyList())).willReturn(mockOptions);

            // when & then
            assertThatThrownBy(() -> lostItemStorageService.getValidRegisterData(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(LostItemFeatureException.INVALID_FEATURE_OPTION.getDetail());
        }
    }

    @Nested
    @DisplayName("새로운 분실물 생성")
    class CreateNewLostItem {

        @Test
        void 기타_카테고리가_아닌_분실물_데이터_저장에_성공해야_한다() {
            // given
            CreateLostItemCommand command = createDummyCommand(VALID_CATEGORY_ID, VALID_FEATURE_ID, VALID_OPTION_ID);
            List<UploadedImageData> uploadedImages = List.of(new UploadedImageData("url1", 1), new UploadedImageData("url2", 2));
            LostItemRegisterData validatedData = new LostItemRegisterData(
                    mockCategory,
                    mockSchoolArea,
                    List.of(Pair.of(mockFeature, mockOptions.get(0)))
            );

            given(lostItemRepository.save(any(LostItem.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            LostItem result = lostItemStorageService.createNewLostItem(command, validatedData, uploadedImages);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.getCategory()).isEqualTo(mockCategory);
                softly.assertThat(result.getFoundArea()).isEqualTo(mockSchoolArea);
                softly.assertThat(result.getStatus()).isEqualTo(LostItemStatus.PENDING);
            });
            then(lostItemRepository).should(times(1)).save(any(LostItem.class));
            then(lostItemImageRepository).should(times(1)).saveAll(anyList());
            then(lostItemFeatureRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        void 기타_카테고리_분실물_데이터_저장에_성공해야_한다() {
            // given
            CreateLostItemCommand command = createETCDummyCommand(VALID_ETC_CATEGORY_ID);
            List<UploadedImageData> uploadedImages = List.of(new UploadedImageData("url1", 1));
            LostItemRegisterData validatedData = new LostItemRegisterData(
                    mockETCCategory,
                    mockSchoolArea,
                    List.of()
            );

            given(lostItemRepository.save(any(LostItem.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            LostItem result = lostItemStorageService.createNewLostItem(command, validatedData, uploadedImages);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.getCategory()).isEqualTo(mockETCCategory);
                softly.assertThat(result.getStatus()).isEqualTo(LostItemStatus.PENDING);
            });
            then(lostItemRepository).should(times(1)).save(any(LostItem.class));
            then(lostItemImageRepository).should(times(1)).saveAll(anyList());
            then(lostItemFeatureRepository).should(never()).saveAll(anyList());
        }
    }

}
