package com.greedy.zupzup.lostitem.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import com.greedy.zupzup.lostitem.repository.RepresentativeImageProjection;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class LostItemViewServiceTest extends ServiceUnitTest {

    private LostItemViewService service;

    @BeforeEach
    void init() {
        service = new LostItemViewService(lostItemRepository, lostItemImageRepository);
    }

    @Test
    void 분실물_목록을_조회할_수_있다() {
        // given
        Long categoryId = null;
        Long areaId = null;

        LostItemListProjection p1 = new ListProj(
                1L, 10L, "전자기기", "https://icon.com/electronics.svg",
                100L, "AI센터", "AI센터 B205", LocalDateTime.now().minusDays(1)
        );
        LostItemListProjection p2 = new ListProj(
                2L, 10L, "전자기기", "https://icon.com/electronics.svg",
                100L, "AI센터", "AI센터 B206", LocalDateTime.now().minusHours(2)
        );

        Page<LostItemListProjection> page = new PageImpl<>(List.of(p1, p2));
        given(lostItemRepository.findList(
                isNull(), isNull(), eq(LostItemStatus.REGISTERED), any(Pageable.class)
        )).willReturn(page);

        // when
        Page<LostItemListCommand> result = service.getLostItems(categoryId, areaId, 1, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(2);
            softly.assertThat(result.getContent().get(0).categoryName()).isEqualTo("전자기기");
            softly.assertThat(result.getContent().get(0).schoolAreaName()).isEqualTo("AI센터");
        });
    }

    @Test
    void 목록_조회에_카테고리_및_구역_필터가_전달된다() {
        // given
        Long categoryId = 99L;
        Long areaId = 777L;

        Page<LostItemListProjection> empty = new PageImpl<>(List.of());
        given(lostItemRepository.findList(
                eq(categoryId), eq(areaId), eq(LostItemStatus.REGISTERED), any(Pageable.class)
        )).willReturn(empty);

        // when
        Page<LostItemListCommand> result = service.getLostItems(categoryId, areaId, 2, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getContent()).hasSize(0);
        });
        then(lostItemRepository).should()
                .findList(eq(categoryId), eq(areaId), eq(LostItemStatus.REGISTERED), any(Pageable.class));
    }


    @Test
    void 단건_조회_카테고리_아이콘이_있으면_그_URL을_대표이미지로_준다() {
        // given
        Long itemId = 1L;

        Category category = mock(
                Category.class);
        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("전자기기");
        when(category.getIconUrl()).thenReturn("https://icon.com/electronics.svg");

        SchoolArea area = mock(
                SchoolArea.class);
        when(area.getId()).thenReturn(100L);
        when(area.getAreaName()).thenReturn("AI센터");

        LostItem item = mock(LostItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getStatus()).thenReturn(LostItemStatus.REGISTERED);
        when(item.getCategory()).thenReturn(category);
        when(item.getFoundArea()).thenReturn(area);
        when(item.getFoundAreaDetail()).thenReturn("AI센터 B205");
        when(item.getCreatedAt()).thenReturn(LocalDateTime.now());

        given(lostItemRepository.getWithCategoryById(itemId)).willReturn(item);

        // when
        LostItemSimpleViewCommand view = service.getSimpleView(itemId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(view.id()).isEqualTo(itemId);
            softly.assertThat(view.categoryName()).isEqualTo("전자기기");
            softly.assertThat(view.representativeImageUrl()).isEqualTo("https://icon.com/electronics.svg");
        });
        then(lostItemImageRepository).should(never()).findRepresentativeImages(any());
    }

    @Test
    void 단건_조회_카테고리가_기타면_아이콘이_있어도_0번_이미지_URL을_대표로_사용한다() {
        // given
        Long itemId = 3L;

        Category category = mock(
                Category.class);
        when(category.getId()).thenReturn(12L);
        when(category.getName()).thenReturn("기타");
        when(category.getIconUrl()).thenReturn("https://icon.com/etc.svg");

        SchoolArea area = mock(
                SchoolArea.class);
        when(area.getId()).thenReturn(102L);
        when(area.getAreaName()).thenReturn("도서관");

        LostItem item = mock(LostItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getStatus()).thenReturn(LostItemStatus.REGISTERED);
        when(item.getCategory()).thenReturn(category);
        when(item.getFoundArea()).thenReturn(area);
        when(item.getFoundAreaDetail()).thenReturn("도서관 1층");
        when(item.getCreatedAt()).thenReturn(LocalDateTime.now());

        String firstImageUrl = "https://images.example.com/lost/" + itemId + "/0.jpg";

        given(lostItemRepository.getWithCategoryById(itemId)).willReturn(item);
        given(lostItemImageRepository.findRepresentativeImages(List.of(itemId)))
                .willReturn(List.of(new RepImg(itemId, firstImageUrl)));

        // when
        LostItemSimpleViewCommand view = service.getSimpleView(itemId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(view.id()).isEqualTo(itemId);
            softly.assertThat(view.representativeImageUrl()).isEqualTo(firstImageUrl);
        });
        then(lostItemImageRepository).should().findRepresentativeImages(List.of(itemId));
    }

    @Test
    void 대표이미지맵_조회는_id대비_URL을_반환한다() {
        // given
        List<Long> ids = List.of(1L, 2L);
        given(lostItemImageRepository.findRepresentativeImages(ids))
                .willReturn(List.of(
                        new RepImg(1L, "https://img.com/1.jpg"),
                        new RepImg(2L, "https://img.com/2.jpg")
                ));

        // when
        Map<Long, String> map = service.getRepresentativeImageMapByItemIds(ids);

        // then
        assertSoftly(softly -> {
            softly.assertThat(map).hasSize(2);
            softly.assertThat(map.get(1L)).isEqualTo("https://img.com/1.jpg");
            softly.assertThat(map.get(2L)).isEqualTo("https://img.com/2.jpg");
        });
    }

    private record ListProj(
            Long id,
            Long categoryId,
            String categoryName,
            String categoryIconUrl,
            Long schoolAreaId,
            String schoolAreaName,
            String foundAreaDetail,
            LocalDateTime createdAt
    ) implements LostItemListProjection {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Long getCategoryId() {
            return categoryId;
        }

        @Override
        public String getCategoryName() {
            return categoryName;
        }

        @Override
        public String getCategoryIconUrl() {
            return categoryIconUrl;
        }

        @Override
        public Long getSchoolAreaId() {
            return schoolAreaId;
        }

        @Override
        public String getSchoolAreaName() {
            return schoolAreaName;
        }

        @Override
        public String getFoundAreaDetail() {
            return foundAreaDetail;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
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
