package com.greedy.zupzup.admin.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.then;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemRepository;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;

import java.time.LocalDateTime;
import java.util.List;

import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.category.domain.FeatureOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AdminLostItemServiceTest {

    private AdminLostItemService service;
    private AdminLostItemRepository adminLostItemRepository;
    private LostItemImageRepository lostItemImageRepository;
    private LostItemFeatureRepository lostItemFeatureRepository;
    private S3ImageFileManager fileManager;

    @BeforeEach
    void init() {
        adminLostItemRepository = mock(AdminLostItemRepository.class);
        lostItemImageRepository = mock(LostItemImageRepository.class);
        lostItemFeatureRepository = mock(LostItemFeatureRepository.class);
        fileManager = mock(S3ImageFileManager.class);

        service = new AdminLostItemService(
                adminLostItemRepository,
                lostItemImageRepository,
                fileManager,
                lostItemFeatureRepository
        );
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
    void 일괄_삭제시_이미지삭제_및_DB삭제한다() {
        List<Long> ids = List.of(1L, 2L);
        RejectLostItemsRequest req = new RejectLostItemsRequest(ids);

        given(lostItemImageRepository.findImageKeysByLostItemIds(ids))
                .willReturn(List.of("k1", "k2"));
        given(adminLostItemRepository.deleteBulkByIds(ids)).willReturn(2);

        RejectLostItemsResponse res = service.rejectBulk(req);

        assertSoftly(s -> {
            s.assertThat(res.successfulCount()).isEqualTo(2);
        });

        then(fileManager).should().delete("k1");
        then(fileManager).should().delete("k2");
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

        // features
        FeatureOption fopt = mock(FeatureOption.class);
        given(fopt.getId()).willReturn(100L);
        given(fopt.getOptionValue()).willReturn("삼성");

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

            FeatureOptionDto dto = res.items().get(0).featureOptions().get(0);
            s.assertThat(dto.optionValue()).isEqualTo("삼성");
        });
    }
}
