package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.member.domain.Member;

public class PledgeFixture {

    public static Pledge PLEDGE(Member owner, LostItem lostItem) {
        return Pledge.builder()
                .owner(owner)
                .lostItem(lostItem)
                .build();
    }
}
