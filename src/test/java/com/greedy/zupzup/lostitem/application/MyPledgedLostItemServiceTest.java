package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListResponse;
import com.greedy.zupzup.lostitem.repository.MyPledgedLostItemProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class MyPledgedLostItemServiceTest extends ServiceUnitTest {

    private MyPledgedLostItemService service;

    @BeforeEach
    void init() {
        service = new MyPledgedLostItemService(lostItemRepository);
    }

    @Test
    void 내가_서약한_분실물_목록을_조회할_수_있다() {
        Long memberId = 1L;

        MyPledgedLostItemProjection p1 = projection(
                101L, 10L, "전자기기",
                100L, "AI센터", "AI센터 B205",
                "https://img.com/101.jpg", LocalDateTime.now().minusDays(1), "보관소 A"
        );

        MyPledgedLostItemProjection p2 = projection(
                102L, 12L, "지갑",
                100L, "AI센터", "AI센터 B207",
                "https://img.com/102.jpg", LocalDateTime.now().minusHours(2), "보관소 B"
        );

        Page<MyPledgedLostItemProjection> pageResult =
                new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10), 2);

        given(lostItemRepository.findPledgedLostItemsByMemberId(memberId, PageRequest.of(0, 10)))
                .willReturn(pageResult);

        // when
        LostItemListResponse response = service.getMyPledgedLostItems(memberId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.count()).isEqualTo(2);
            softly.assertThat(response.items()).hasSize(2);

            MyPledgedListResponse firstItem = (MyPledgedListResponse) response.items().get(0);
            softly.assertThat(firstItem.representativeImageUrl()).isNotBlank();
        });

        then(lostItemRepository).should().findPledgedLostItemsByMemberId(memberId, PageRequest.of(0, 10));
    }

    private MyPledgedLostItemProjection projection(
            Long id, Long categoryId, String categoryName,
            Long schoolAreaId, String schoolAreaName, String foundAreaDetail,
            String imageUrl, LocalDateTime pledgedAt, String depositArea
    ) {
        return new MyPledgedLostItemProjection() {
            public Long getId() { return id; }
            public Long getCategoryId() { return categoryId; }
            public String getCategoryName() { return categoryName; }
            public Long getSchoolAreaId() { return schoolAreaId; }
            public String getSchoolAreaName() { return schoolAreaName; }
            public String getFoundAreaDetail() { return foundAreaDetail; }
            public LocalDateTime getCreatedAt() { return LocalDateTime.now(); }
            public String getRepresentativeImageUrl() { return imageUrl; }
            public LocalDateTime getPledgedAt() { return pledgedAt; }
            public String getDepositArea() { return depositArea; }
        };
    }
}
