package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import org.mindrot.jbcrypt.BCrypt;

public class MemberFixture {

    public static Member MEMBER() {
        return Member.builder()
                .studentId(123456)
                .password("asd")
                .role(Role.USER)
                .build();
    }

    /**
     * 암호화된 비밀번호가 필요한 DB 저장 및 API 인수 테스트용
     */
    public static Member MEMBER_WITH_ENCODED_PASSWORD(String password) {
        return Member.builder()
                .studentId(123456)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .role(Role.USER)
                .build();
    }

    public static Member ADMIN_WITH_ENCODED_PASSWORD(String password) {
        return Member.builder()
                .studentId(123456)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .role(Role.ADMIN)
                .build();
    }
}

