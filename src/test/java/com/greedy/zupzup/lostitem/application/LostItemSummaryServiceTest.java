package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.LostItemSummaryCommand;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemRepository.LostItemSummaryProjection;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class LostItemSummaryServiceTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemSummaryService service;

    private record SummaryProj(Long schoolAreaId, String schoolAreaName, Long lostCount)
            implements LostItemSummaryProjection {
        @Override
        public Long getSchoolAreaId() {
            return schoolAreaId;
        }

        @Override
        public String getSchoolAreaName() {
            return schoolAreaName;
        }

        @Override
        public Long getLostCount() {
            return lostCount;
        }
    }

    @Nested
    class GetSummary {

        @Test
        void 구역별_요약_조회는_REGISTERED만_집계하고_목록을_반환한다() {
            // given
            List<LostItemSummaryProjection> rows = List.of(
                    new SummaryProj(1L, "AI 센터", 3L),
                    new SummaryProj(2L, "학생회관", 1L)
            );

            given(lostItemRepository.findAreaSummaries(
                    isNull(), eq(LostItemStatus.REGISTERED)))
                    .willReturn(rows);

            // when
            List<LostItemSummaryCommand> result = service.getSummary(null);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result.get(0).schoolAreaId()).isEqualTo(1L);
                softly.assertThat(result.get(0).schoolAreaName()).isEqualTo("AI 센터");
                softly.assertThat(result.get(0).lostCount()).isEqualTo(3L);
                softly.assertThat(result.get(1).schoolAreaId()).isEqualTo(2L);
                softly.assertThat(result.get(1).schoolAreaName()).isEqualTo("학생회관");
                softly.assertThat(result.get(1).lostCount()).isEqualTo(1L);
            });

            verify(lostItemRepository).findAreaSummaries(null, LostItemStatus.REGISTERED);
        }

        @Test
        void 카테고리_필터가_있으면_해당_카테고리인_분실물_목록을_반환한다() {
            // given
            Long categoryId = 100L;
            List<LostItemSummaryProjection> rows =
                    List.of(new SummaryProj(3L, "도서관", 5L));

            given(lostItemRepository.findAreaSummaries(
                    eq(categoryId), eq(LostItemStatus.REGISTERED)))
                    .willReturn(rows);
            // when
            List<LostItemSummaryCommand> result = service.getSummary(categoryId);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).schoolAreaId()).isEqualTo(3L);
                softly.assertThat(result.get(0).schoolAreaName()).isEqualTo("도서관");
                softly.assertThat(result.get(0).lostCount()).isEqualTo(5L);
            });

            verify(lostItemRepository).findAreaSummaries(categoryId, LostItemStatus.REGISTERED);
        }

        @Test
        void 결과가_없으면_빈_목록을_반환한다() {
            // given
            given(lostItemRepository.findAreaSummaries(null, LostItemStatus.REGISTERED))
                    .willReturn(List.of());

            // when
            List<LostItemSummaryCommand> result = service.getSummary(null);

            // then
            assertSoftly(softly -> softly.assertThat(result).isEmpty());

            verify(lostItemRepository).findAreaSummaries(null, LostItemStatus.REGISTERED);
        }
    }
}
