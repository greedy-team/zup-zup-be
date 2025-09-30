package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.repository.RepresentativeImageProjection;
import com.greedy.zupzup.pledge.application.PledgeQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class MyPledgedLostItemServiceTest extends ServiceUnitTest {
    private MyPledgedLostItemService service;
    private PledgeQueryService pledgeQueryService;
    private LostItemViewService lostItemViewService;

    @BeforeEach
    void init() {
        pledgeQueryService = mock(PledgeQueryService.class);
        lostItemViewService = mock(LostItemViewService.class);
        service = new MyPledgedLostItemService(pledgeQueryService, lostItemViewService);
    }

    @Test
    void 내가_서약한_분실물_목록을_조회할_수_있다() {
        // given
        Long memberId = 1L;

        Page<Long> pledgedIds = new PageImpl<>(List.of(101L, 102L), PageRequest.of(0, 10), 2);
        given(pledgeQueryService.getPledgedLostItemIds(memberId, 1, 10))
                .willReturn(pledgedIds);

        LostItemListCommand c1 = new LostItemListCommand(
                101L, 10L, "전자기기", "https://icon.com/e.svg",
                100L, "AI센터", "AI센터 B205", LocalDateTime.now().minusDays(1)
        );
        LostItemListCommand c2 = new LostItemListCommand(
                102L, 12L, "지갑", "https://icon.com/wallet.svg",
                100L, "AI센터", "AI센터 B207", LocalDateTime.now().minusHours(2)
        );

        Page<LostItemListCommand> pageResult =
                new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);

        given(lostItemViewService.getLostItemsByIds(List.of(101L, 102L), 1, 10))
                .willReturn(pageResult);

        given(lostItemViewService.getRepresentativeImageMapByItemIds(List.of(101L, 102L)))
                .willReturn(Map.of(
                        101L, "https://img.com/101.jpg",
                        102L, "https://img.com/102.jpg"
                ));

        // when
        LostItemListResponse response = service.getMyPledgedLostItems(memberId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.count()).isEqualTo(2);
            softly.assertThat(response.items()).hasSize(2);
            softly.assertThat(response.items().get(0).representativeImageUrl()).isNotBlank();
        });

        then(pledgeQueryService).should().getPledgedLostItemIds(memberId, 1, 10);
        then(lostItemViewService).should().getLostItemsByIds(List.of(101L, 102L), 1, 10);
        then(lostItemViewService).should().getRepresentativeImageMapByItemIds(List.of(101L, 102L));
    }

    @Test
    void 서약한_분실물이_없으면_빈_리스트를_반환한다() {
        // given
        Long memberId = 2L;

        Page<Long> pledgedIds = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(pledgeQueryService.getPledgedLostItemIds(memberId, 1, 10))
                .willReturn(pledgedIds);

        Page<LostItemListCommand> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(lostItemViewService.getLostItemsByIds(List.of(), 1, 10))
                .willReturn(emptyPage);

        given(lostItemViewService.getRepresentativeImageMapByItemIds(List.of()))
                .willReturn(Map.of());

        // when
        LostItemListResponse response = service.getMyPledgedLostItems(memberId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.count()).isEqualTo(0);
            softly.assertThat(response.items()).isEmpty();
        });
    }

    private record RepImg(Long lostItemId, String imageUrl)
            implements RepresentativeImageProjection {
        @Override
        public Long getLostItemId() {
            return lostItemId;
        }

        @Override
        public String getImageUrl() {
            return imageUrl;
        }
    }

}
