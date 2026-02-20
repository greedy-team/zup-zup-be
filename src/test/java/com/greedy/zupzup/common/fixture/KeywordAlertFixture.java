package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.member.domain.Member;

public class KeywordAlertFixture {

    public static KeywordAlert KEYWORD_ALERT(Member member, Category category) {
        return KeywordAlert.subscribe(member, category);
    }
}
