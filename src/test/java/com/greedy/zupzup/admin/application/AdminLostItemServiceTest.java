package com.greedy.zupzup.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.admin.lostitem.application.dto.AdminFeatureOptionDto;
import com.greedy.zupzup.admin.lostitem.application.dto.ItemImageBulkDeletedEvent;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemRequest;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;

import com.greedy.zupzup.schoolarea.domain.SchoolArea;

import java.time.LocalDateTime;
import java.util.List;

import com.greedy.zupzup.category.domain.FeatureOption;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public class AdminLostItemServiceTest extends ServiceUnitTest {

    @InjectMocks
    private AdminLostItemService service;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private void setupValidRegisterData() {
        LostItemRegisterData mockData = mock(LostItemRegisterData.class);
        given(mockData.foundSchoolArea()).willReturn(mock(SchoolArea.class));
        given(mockData.category()).willReturn(mock(Category.class));
        given(lostItemStorageService.getValidRegisterData(any())).willReturn(mockData);
    }

    private LostItem stubItem(
            Long id, String desc, String deposit, String foundDetail,
            Long categoryId, String categoryName, Long schoolAreaId, String schoolAreaName) {

        Category category = mock(Category.class);
        given(category.getId()).willReturn(categoryId);
        given(category.getName()).willReturn(categoryName);

        SchoolArea area = mock(SchoolArea.class);
        given(area.getId()).willReturn(schoolAreaId);
        given(area.getAreaName()).willReturn(schoolAreaName);

        LostItem item = mock(LostItem.class);
        given(item.getId()).willReturn(id);
        given(item.getDescription()).willReturn(desc);
        given(item.getDepositArea()).willReturn(deposit);
        given(item.getFoundAreaDetail()).willReturn(foundDetail);
        given(item.getCategory()).willReturn(category);
        given(item.getFoundArea()).willReturn(area);
        given(item.getCreatedAt()).willReturn(LocalDateTime.now());

        return item;
    }

    @Test
    void 일괄_승인시_PENDING_REGISTERED로_변경한다() {
        List<Long> ids = List.of(1L, 2L);
        ApproveLostItemsRequest req = new ApproveLostItemsRequest(ids);

        given(adminLostItemRepository.updateStatusBulkByIds(ids, LostItemStatus.REGISTERED, LostItemStatus.PENDING))
                .willReturn(2);

        ApproveLostItemsResponse res = service.approveBulk(req);

        assertSoftly(s -> {
            s.assertThat(res.successfulCount()).isEqualTo(2);
            s.assertThat(res.totalRequestedCount()).isEqualTo(2);
        });
    }

    @Test
    void 일괄_삭제시_DB삭제_및_이벤트를_발행한다() { // [수정] 테스트 이름
        // given
        List<Long> ids = List.of(1L, 2L);
        RejectLostItemsRequest req = new RejectLostItemsRequest(ids);
        List<String> expectedImageUrls = List.of("k1", "k2");

        given(lostItemImageRepository.findImageKeysByLostItemIds(ids))
                .willReturn(expectedImageUrls);
        given(adminLostItemRepository.deleteBulkByIds(ids)).willReturn(2);

        // when
        RejectLostItemsResponse res = service.rejectBulk(req);

        // then
        // 1. 응답 결과 검증
        assertSoftly(s -> {
            s.assertThat(res.successfulCount()).isEqualTo(2);
        });

        // 2. DB 삭제 로직 호출 검증
        then(lostItemFeatureRepository).should().deleteByLostItemIds(ids);
        then(lostItemImageRepository).should().deleteByLostItemIds(ids);
        then(adminLostItemRepository).should().deleteBulkByIds(ids);

        // 4. 이벤트 발행 검증
        ArgumentCaptor<ItemImageBulkDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(ItemImageBulkDeletedEvent.class);

        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        ItemImageBulkDeletedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.imageUrls()).isEqualTo(expectedImageUrls);
    }

    @Test
    void 보류중_분실물_목록을_조회한다() {
        int page = 1, limit = 10;
        Pageable pageable = PageRequest.of(page - 1, limit);

        LostItem i1 = stubItem(1L, "아이폰", "학생회관", "도서관3층", 10L, "전자제품", 1L, "AI센터");
        LostItem i2 = stubItem(2L, "지갑", "경비실", "운동장", 11L, "지갑", 2L, "정문");

        given(adminLostItemRepository.findPendingItems(LostItemStatus.PENDING, pageable))
                .willReturn(List.of(i1, i2));

        LostItemImage img1 = mock(LostItemImage.class);
        given(img1.getLostItem()).willReturn(i1);
        given(img1.getImageKey()).willReturn("img1");

        LostItemImage img2 = mock(LostItemImage.class);
        given(img2.getLostItem()).willReturn(i2);
        given(img2.getImageKey()).willReturn("img2");

        given(lostItemImageRepository.findImagesForItems(List.of(1L, 2L)))
                .willReturn(List.of(img1, img2));

        Feature feature = mock(Feature.class);
        given(feature.getQuizQuestion()).willReturn("제조사는 무엇인가요?");

        FeatureOption fopt = mock(FeatureOption.class);
        given(fopt.getId()).willReturn(100L);
        given(fopt.getOptionValue()).willReturn("삼성");
        given(fopt.getFeature()).willReturn(feature);

        LostItemFeature lf = mock(LostItemFeature.class);
        given(lf.getLostItem()).willReturn(i1);
        given(lf.getSelectedOption()).willReturn(fopt);

        given(lostItemFeatureRepository.findFeaturesForLostItems(List.of(1L, 2L)))
                .willReturn(List.of(lf));

        AdminPendingLostItemListResponse res = service.getPendingLostItems(page, limit);

        assertSoftly(s -> {
            s.assertThat(res.count()).isEqualTo(2);
            s.assertThat(res.items()).hasSize(2);
            s.assertThat(res.items().get(0).id()).isEqualTo(1L);
            s.assertThat(res.items().get(0).imageUrl()).contains("img1");

            AdminFeatureOptionDto dto = res.items().get(0).featureOptions().get(0);
            s.assertThat(dto.optionValue()).isEqualTo("삼성");
            s.assertThat(dto.quizQuestion()).isEqualTo("제조사는 무엇인가요?");
        });
    }


    private UpdateLostItemRequest validRequest() {
        return new UpdateLostItemRequest(
                "설명 수정",
                "보관 장소",
                1L,
                "발견 위치",
                10L,
                List.of() // featureOptions
        );
    }

    private LostItem pendingLostItem(Long id) {
        LostItem item = mock(LostItem.class);
        given(item.getId()).willReturn(id);
        given(item.getStatus()).willReturn(LostItemStatus.PENDING);
        return item;
    }

    @Test
    void 관리자_분실물_수정시_기존이미지만_유지하고_정보를_수정해도_승인된다() {
        // given
        Long lostItemId = 1L;
        LostItem item = pendingLostItem(lostItemId);
        setupValidRegisterData();

        given(adminLostItemRepository.findById(lostItemId)).willReturn(Optional.of(item));

        List<Long> keepImageIds = List.of(10L);
        given(lostItemImageRepository.countByIdInAndLostItemId(keepImageIds, lostItemId)).willReturn(1L);
        given(lostItemImageRepository.findByLostItemId(lostItemId)).willReturn(List.of(mock(LostItemImage.class)));

        // when
        service.updateLostItem(lostItemId, validRequest(), keepImageIds, List.of());

        // then
        then(item).should().approve();
    }

    @Test
    void 관리자_분실물_수정시_기존이미지는_유지하고_새이미지를_추가한뒤_승인한다() {
        // given
        Long lostItemId = 1L;
        LostItem item = pendingLostItem(lostItemId);
        setupValidRegisterData();

        given(adminLostItemRepository.findById(lostItemId)).willReturn(Optional.of(item));

        List<Long> keepImageIds = List.of(10L);
        given(lostItemImageRepository.countByIdInAndLostItemId(keepImageIds, lostItemId)).willReturn(1L);
        given(lostItemImageRepository.findByLostItemId(lostItemId)).willReturn(List.of(mock(LostItemImage.class)));
        given(s3ImageFileManager.upload(any(), any())).willReturn("new-img-url");

        // when
        service.updateLostItem(lostItemId, validRequest(), keepImageIds, List.of(mock(MultipartFile.class)));

        // then
        then(item).should().approve();
        then(lostItemImageRepository).should().saveAll(any());
    }
}
