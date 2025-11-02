package com.greedy.zupzup.admin.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdminLostItemServiceTest extends ServiceUnitTest {

    private AdminLostItemService service;

    private LostItemFeatureRepository lostItemFeatureRepository;
    private LostItemImageRepository lostItemImageRepository;
    private S3ImageFileManager s3ImageFileManager;
    private LostItemRepository lostItemRepository;

    @BeforeEach
    void init() {
        lostItemFeatureRepository = mock(LostItemFeatureRepository.class);
        lostItemImageRepository = mock(LostItemImageRepository.class);
        s3ImageFileManager = mock(S3ImageFileManager.class);
        lostItemRepository = mock(LostItemRepository.class);

        service = new AdminLostItemService(lostItemRepository, lostItemImageRepository, s3ImageFileManager, lostItemFeatureRepository);
    }

    private final List<Long> testIds = List.of(1L, 2L);

    @Test
    void 일괄_승인_시_PENDING_상태를_REGISTERED로_변경한다() {
        // given
        ApproveLostItemsRequest request = new ApproveLostItemsRequest(testIds);
        given(lostItemRepository.updateStatusBulkByIds(
                eq(testIds), eq(LostItemStatus.REGISTERED), eq(LostItemStatus.PENDING)
        )).willReturn(2);

        // when
        ApproveLostItemsResponse response = service.approveBulk(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.successfulCount()).isEqualTo(2);
            softly.assertThat(response.totalRequestedCount()).isEqualTo(2);
        });
        then(lostItemRepository).should().updateStatusBulkByIds(eq(testIds), eq(LostItemStatus.REGISTERED), eq(LostItemStatus.PENDING));
    }

    @Test
    void 일괄_삭제시_DB에서_데이터를_삭제한다() {
        // given
        RejectLostItemsRequest request = new RejectLostItemsRequest(testIds);
        List<String> mockImageKeys = List.of("key1", "key2");

        given(lostItemImageRepository.findImageKeysByLostItemIds(eq(testIds))).willReturn(mockImageKeys);
        given(lostItemRepository.deleteBulkByIds(eq(testIds))).willReturn(2);

        // when
        RejectLostItemsResponse response = service.rejectBulk(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.successfulCount()).isEqualTo(2);
            softly.assertThat(response.totalRequestedCount()).isEqualTo(2);
        });

        then(s3ImageFileManager).should().delete("key1");
        then(s3ImageFileManager).should().delete("key2");

        then(lostItemFeatureRepository).should().deleteByLostItemIds(eq(testIds));
        then(lostItemImageRepository).should().deleteByLostItemIds(eq(testIds));

        then(lostItemRepository).should().deleteBulkByIds(eq(testIds));
    }
}
