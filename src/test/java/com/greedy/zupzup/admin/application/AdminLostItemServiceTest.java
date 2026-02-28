package com.greedy.zupzup.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.admin.lostitem.application.dto.ItemImageBulkDeletedEvent;
import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemResult;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemCommand;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.application.dto.UploadedImageData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AdminLostItemServiceTest extends ServiceUnitTest {

    @InjectMocks
    private AdminLostItemService service;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private void setupValidRegisterData() {
        LostItemRegisterData mockData = mock(LostItemRegisterData.class);
        lenient().when(mockData.foundSchoolArea()).thenReturn(mock(SchoolArea.class));
        lenient().when(mockData.category()).thenReturn(mock(Category.class));

        given(lostItemStorageService.getValidUpdateData(any(UpdateLostItemCommand.class)))
                .willReturn(mockData);
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
    void 일괄_삭제시_DB삭제_및_이벤트를_발행한다() {
        List<Long> ids = List.of(1L, 2L);
        RejectLostItemsRequest req = new RejectLostItemsRequest(ids);
        List<String> expectedImageUrls = List.of("k1", "k2");

        given(lostItemImageRepository.findImageKeysByLostItemIds(ids))
                .willReturn(expectedImageUrls);
        given(adminLostItemRepository.deleteBulkByIds(ids)).willReturn(2);

        RejectLostItemsResponse res = service.rejectBulk(req);

        assertSoftly(s -> {
            s.assertThat(res.successfulCount()).isEqualTo(2);
        });

        then(lostItemFeatureRepository).should().deleteByLostItemIds(ids);
        then(lostItemImageRepository).should().deleteByLostItemIds(ids);
        then(adminLostItemRepository).should().deleteBulkByIds(ids);

        ArgumentCaptor<ItemImageBulkDeletedEvent> captor =
                ArgumentCaptor.forClass(ItemImageBulkDeletedEvent.class);

        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().imageUrls()).isEqualTo(expectedImageUrls);
    }

    @Test
    void 보류중_분실물_목록을_조회한다() {
        Pageable pageable = PageRequest.of(0, 10);

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

        FeatureOption option = mock(FeatureOption.class);
        given(option.getOptionValue()).willReturn("삼성");
        given(option.getFeature()).willReturn(feature);

        LostItemFeature lf = mock(LostItemFeature.class);
        given(lf.getLostItem()).willReturn(i1);
        given(lf.getSelectedOption()).willReturn(option);

        given(lostItemFeatureRepository.findFeaturesForLostItems(List.of(1L, 2L)))
                .willReturn(List.of(lf));

        AdminPendingLostItemListResponse res = service.getPendingLostItems(1, 10);

        assertSoftly(s -> {
            s.assertThat(res.count()).isEqualTo(2);
            s.assertThat(res.items()).hasSize(2);
        });
    }

    private UpdateLostItemCommand validCommand(Long lostItemId, List<Long> keepImageIds) {
        return new UpdateLostItemCommand(
                lostItemId,
                "설명 수정",
                "보관 장소",
                1L,
                "발견 위치",
                10L,
                List.of(),
                keepImageIds,
                List.of() // newImages
        );
    }
    private LostItem pendingLostItem(Long id) {
        return mock(LostItem.class);
    }


    @Test
    void 관리자_분실물_수정시_S3업로드와_DB트랜잭션을_순차적으로_호출한다() {
        // given
        Long lostItemId = 1L;
        List<Long> keepImageIds = List.of(10L);
        UpdateLostItemCommand command = validCommand(lostItemId, keepImageIds);

        LostItem item = mock(LostItem.class);
        given(item.getStatus()).willReturn(LostItemStatus.PENDING);
        given(adminLostItemRepository.findById(lostItemId)).willReturn(Optional.of(item));

        LostItemImage existingImg = mock(LostItemImage.class);
        given(existingImg.getId()).willReturn(10L);
        given(lostItemImageRepository.findByLostItemId(lostItemId)).willReturn(List.of(existingImg));

        LostItemRegisterData mockRegData = mock(LostItemRegisterData.class);
        given(lostItemStorageService.getValidUpdateData(command)).willReturn(mockRegData);

        List<UploadedImageData> mockUploads = List.of(new UploadedImageData("url", 0));
        given(lostItemStorageService.uploadImages(any())).willReturn(mockUploads);

        List<String> oldKeys = List.of("oldKey");
        given(lostItemStorageService.updateInTransaction(eq(item), eq(command), eq(mockRegData), any(), any()))
                .willReturn(oldKeys);

        // when
        UpdateLostItemResult result = service.updateLostItem(command);

        // then
        assertSoftly(s -> {
            s.assertThat(result.lostItemId()).isEqualTo(lostItemId);
        });

        // 호출 순서 및 위임 검증
        then(lostItemStorageService).should().uploadImages(any());
        then(lostItemStorageService).should().updateInTransaction(eq(item), eq(command), eq(mockRegData), any(), any());
        then(lostItemStorageService).should().deleteOldImages(oldKeys);
    }
}
