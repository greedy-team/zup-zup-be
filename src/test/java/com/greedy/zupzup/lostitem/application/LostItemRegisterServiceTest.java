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
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_COLOR;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_COLOR_OPTIONS;
import static com.greedy.zupzup.common.fixture.SchoolAreaFixture.AI_CENTER;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;


class LostItemRegisterServiceTest extends ServiceUnitTest {

    private static final Long VALID_CATEGORY_ID = 1L;
    private static final Long VALID_ECT_CATEGORY_ID = 2L;
    private static final Long VALID_FEATURE_ID = 2L;
    private static final Long VALID_OPTION_ID = 3L;
    private static final Long VALID_FOUND_AREA_ID = 4L;

    @InjectMocks
    private LostItemRegisterService lostItemRegisterService;

    private Category mockCategory;
    private Category mockECTCategory;
    private Feature mockFeature;
    private List<FeatureOption> mockOptions;
    private SchoolArea mockSchoolArea;

    @BeforeEach
    void setUp() {
        mockCategory = ELECTRONIC();
        mockECTCategory = CategoryFixture.ETC();
        mockFeature = ELECTRONIC_COLOR(mockCategory);
        mockOptions = ELECTRONIC_COLOR_OPTIONS(mockFeature);
        mockSchoolArea = AI_CENTER();

        ReflectionTestUtils.setField(mockCategory, "id", VALID_CATEGORY_ID);
        ReflectionTestUtils.setField(mockECTCategory, "id", VALID_ECT_CATEGORY_ID);
        ReflectionTestUtils.setField(mockFeature, "id", VALID_FEATURE_ID);
        ReflectionTestUtils.setField(mockOptions.get(0), "id", VALID_OPTION_ID);
        ReflectionTestUtils.setField(mockSchoolArea, "id", VALID_FOUND_AREA_ID);
        ReflectionTestUtils.setField(mockCategory, "features", List.of(mockFeature));
    }


    @Test
    void 분실물_등록에_성공하면_생성된_분실물_객체를_반환해야_한다() {

        // given
        LostItemRegisterRequest request = createDummyRequest(VALID_CATEGORY_ID, VALID_FEATURE_ID, VALID_OPTION_ID);
        List<MultipartFile> images = createDummyImages(3);
        CreateLostItemCommand command = CreateLostItemCommand.of(request, images);

        given(categoryRepository.findWithFeaturesById(request.categoryId())).willReturn(Optional.of(mockCategory));
        given(featureOptionRepository.findByFeatureIds(anyList())).willReturn(mockOptions);
        given(schoolAreaRepository.getAreaById(request.foundAreaId())).willReturn(mockSchoolArea);
        given(s3ImageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");

        // when
        LostItem result = lostItemRegisterService.registLostItem(command);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.getCategory()).isEqualTo(mockCategory);
            softly.assertThat(result.getFoundArea()).isEqualTo(mockSchoolArea);
            softly.assertThat(result.getDescription()).isEqualTo("핸드폰 액정이 깨져 있어요.");
            softly.assertThat(result.getStatus()).isEqualTo(LostItemStatus.REGISTERED);
        });

        then(lostItemRepository).should(times(1)).save(any(LostItem.class));
        then(lostItemImageRepository).should(times(1)).saveAll(anyList());
        then(lostItemFeatureRepository).should(times(1)).saveAll(anyList());
        then(s3ImageFileManager).should(times(3)).upload(any(MultipartFile.class), any(String.class));
    }


    @Test
    void 기타_분실물은_특징값_없이_등록에_성공해야_한다() {

        // given
        LostItemRegisterRequest request = createECTDummyRequest(VALID_ECT_CATEGORY_ID);
        List<MultipartFile> images = createDummyImages(3);
        CreateLostItemCommand command = CreateLostItemCommand.of(request, images);

        given(categoryRepository.findWithFeaturesById(request.categoryId())).willReturn(Optional.of(mockECTCategory));
        given(schoolAreaRepository.getAreaById(request.foundAreaId())).willReturn(mockSchoolArea);
        given(s3ImageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");

        // when
        LostItem result = lostItemRegisterService.registLostItem(command);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.getCategory()).isEqualTo(mockECTCategory);
            softly.assertThat(result.getFoundArea()).isEqualTo(mockSchoolArea);
            softly.assertThat(result.getDescription()).isEqualTo("핸드폰 액정이 깨져 있어요.");
            softly.assertThat(result.getStatus()).isEqualTo(LostItemStatus.REGISTERED);
        });

        then(lostItemRepository).should(times(1)).save(any(LostItem.class));
        then(lostItemImageRepository).should(times(1)).saveAll(anyList());
        then(lostItemFeatureRepository).should(never()).saveAll(anyList());
        then(s3ImageFileManager).should(times(3)).upload(any(MultipartFile.class), any(String.class));
    }


    @Test
    void 존재하지_않는_카테고리로_분실물_등록을_요청하면_예외가_발생해야_한다() {

        // given
        Long invalidCategoryId = 99L;
        LostItemRegisterRequest request = createDummyRequest(invalidCategoryId, VALID_FEATURE_ID, VALID_OPTION_ID);
        List<MultipartFile> images = createDummyImages(1);
        CreateLostItemCommand command = CreateLostItemCommand.of(request, images);

        given(categoryRepository.findWithFeaturesById(request.categoryId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(CategoryException.CATEGORY_NOT_FOUND.getDetail());

        then(lostItemRepository).should(never()).save(any(LostItem.class));
        then(lostItemImageRepository).should(never()).saveAll(anyList());
        then(lostItemFeatureRepository).should(never()).saveAll(anyList());
        then(s3ImageFileManager).should(never()).upload(any(MultipartFile.class), any(String.class));
    }

    @Test
    void 카테고리와_일치하지_않는_특징으로_분실물_등록을_요청하면_예외가_발생해야_한다() {

        // given
        Long invalidFeatureId = 99L;
        LostItemRegisterRequest request = createDummyRequest(VALID_CATEGORY_ID, invalidFeatureId, VALID_OPTION_ID);
        CreateLostItemCommand command = CreateLostItemCommand.of(request, createDummyImages(1));

        given(categoryRepository.findWithFeaturesById(request.categoryId())).willReturn(Optional.of(mockCategory));

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(CategoryException.INVALID_CATEGORY_FEATURE.getDetail());

        then(lostItemRepository).should(never()).save(any());
    }

    @Test
    void 특징에_속하지_않는_옵션으로_분실물_등록을_요청하면_예외가_발생해야_한다() {

        // given
        Long invalidOptionId = 99L;
        LostItemRegisterRequest request = createDummyRequest(VALID_CATEGORY_ID, VALID_FEATURE_ID, invalidOptionId);
        CreateLostItemCommand command = CreateLostItemCommand.of(request, createDummyImages(1));

        List<Long> categoryFeatureIds = mockCategory.getFeatures().stream().
                map(Feature::getId)
                .toList();

        given(categoryRepository.findWithFeaturesById(request.categoryId())).willReturn(Optional.of(mockCategory));
        given(featureOptionRepository.findByFeatureIds(categoryFeatureIds)).willReturn(mockOptions);

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemFeatureException.INVALID_FEATURE_OPTION.getDetail());

        then(lostItemRepository).should(never()).save(any());
    }

    private LostItemRegisterRequest createDummyRequest(Long categoryId, Long featureId, Long optionId) {
        return new LostItemRegisterRequest(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                VALID_FOUND_AREA_ID,
                "AI 센터 B205",
                categoryId,
                List.of(new ItemFeatureRequest(featureId, optionId))
        );
    }

    private LostItemRegisterRequest createECTDummyRequest(Long categoryId) {
        return new LostItemRegisterRequest(
                "핸드폰 액정이 깨져 있어요.",
                "학술 정보원 2층 데스크",
                VALID_FOUND_AREA_ID,
                "AI 센터 B205",
                categoryId,
                List.of()
        );
    }

    private List<MultipartFile> createDummyImages(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new MockMultipartFile("images", "image" + i + ".jpg", "image/jpeg", new byte[]{}))
                .collect(Collectors.toList());
    }

}
