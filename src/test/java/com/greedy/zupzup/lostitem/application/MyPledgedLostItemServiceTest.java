package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.pledge.application.PledgeQueryService;
import java.time.LocalDateTime;
import java.util.List;
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

        MyPledgedLostItemCommand c1 = new MyPledgedLostItemCommand(
                101L, 10L, "전자기기", "https://icon.com/e.svg",
                100L, "AI센터", "AI센터 B205", LocalDateTime.now().minusDays(1),
                "https://img.com/101.jpg"
        );
        MyPledgedLostItemCommand c2 = new MyPledgedLostItemCommand(
                102L, 12L, "지갑", "https://icon.com/wallet.svg",
                100L, "AI센터", "AI센터 B207", LocalDateTime.now().minusHours(2),
                "https://img.com/102.jpg"
        );

        Page<MyPledgedLostItemCommand> pageResult =
                new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);

        given(lostItemViewService.getPledgedLostItems(List.of(101L, 102L), 1, 10))
                .willReturn(pageResult);

        // when
        LostItemListResponse response = service.getMyPledgedLostItems(memberId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.count()).isEqualTo(2);
            softly.assertThat(response.items()).hasSize(2);
            softly.assertThat(response.items().get(0).representativeImageUrl()).isNotBlank();
        });

        then(pledgeQueryService).should().getPledgedLostItemIds(memberId, 1, 10);
        then(lostItemViewService).should().getPledgedLostItems(List.of(101L, 102L), 1, 10);
    }

    @Test
    void 서약한_분실물이_없으면_빈_리스트를_반환한다() {
        // given
        Long memberId = 2L;

        Page<Long> pledgedIds = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(pledgeQueryService.getPledgedLostItemIds(memberId, 1, 10))
                .willReturn(pledgedIds);

        Page<MyPledgedLostItemCommand> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        given(lostItemViewService.getPledgedLostItems(List.of(), 1, 10))
                .willReturn(emptyPage);

        // when
        LostItemListResponse response = service.getMyPledgedLostItems(memberId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.count()).isEqualTo(0);
            softly.assertThat(response.items()).isEmpty();
        });
    }
}
