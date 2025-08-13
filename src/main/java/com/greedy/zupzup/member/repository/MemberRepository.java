package com.greedy.zupzup.member.repository;

import com.greedy.zupzup.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
