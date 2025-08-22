package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;

public class MemberFixture {

    public static Member MEMBER() {
        return Member.builder()
                .name("테스트유저")
                .studentId(123456)
                .role(Role.USER)
                .build();
    }
}

