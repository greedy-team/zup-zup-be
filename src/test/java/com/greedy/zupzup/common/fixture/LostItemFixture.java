package com.greedy.zupzup.common.fixture;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.ETC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.WALLET;
import static com.greedy.zupzup.common.fixture.LostItemImageFixture.DEFAULT_IMAGE;
import static com.greedy.zupzup.common.fixture.SchoolAreaFixture.AI_CENTER;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class LostItemFixture {

    // 퀴즈 생성이 가능한 분실물 (전자기기)
    public static LostItem PLEDGEABLE_ELECTRONIC_LOST_ITEM() {
        LostItem lostItem = LostItem.builder()
                .category(ELECTRONIC())
                .foundArea(AI_CENTER())
                .status(LostItemStatus.REGISTERED) 
                .build();
        ReflectionTestUtils.setField(lostItem, "id", 1L);

        List<LostItemImage> images = List.of(DEFAULT_IMAGE(lostItem));
        ReflectionTestUtils.setField(lostItem, "images", images);

        return lostItem;
    }

    // 이미 수령 신청된 분실물
    public static LostItem ALREADY_PLEDGED_LOST_ITEM() {
        LostItem lostItem = LostItem.builder()
                .category(ELECTRONIC())
                .foundArea(AI_CENTER())
                .status(LostItemStatus.PLEDGED) 
                .build();
        ReflectionTestUtils.setField(lostItem, "id", 2L);

        List<LostItemImage> images = List.of(DEFAULT_IMAGE(lostItem));
        ReflectionTestUtils.setField(lostItem, "images", images);

        return lostItem;
    }

    // 퀴즈 생성이 불가능한 분실물 (지갑)
    public static LostItem NON_QUIZ_CATEGORY_LOST_ITEM() {
        LostItem lostItem = LostItem.builder()
                .category(ETC())
                .foundArea(AI_CENTER())
                .status(LostItemStatus.REGISTERED)
                .build();
        ReflectionTestUtils.setField(lostItem, "id", 3L);

        List<LostItemImage> images = List.of(DEFAULT_IMAGE(lostItem));
        ReflectionTestUtils.setField(lostItem, "images", images);

        return lostItem;
    }
}
