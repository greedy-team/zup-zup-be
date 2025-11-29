package com.greedy.zupzup.member.domain;

import com.greedy.zupzup.global.BaseTimeEntity;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer studentId;

    // 일단은 nullable로 -> 추후 사이트 로그인 방식으로 바뀌면 가입 유도
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

}
