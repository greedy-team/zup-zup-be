package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Provider;
import com.greedy.zupzup.member.domain.Role;

public class MemberFixture {

    public static Member MEMBER() {
        return Member.builder()
                .email("testuser@naver.com")
                .nickname("테스트유저")
                .provider(Provider.NAVER)
                .providerId("naver-test-id-12345")
                .role(Role.USER)
                .emailConsent(true)
                .build();
    }
}

