package com.greedy.zupzup.member.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.exception.MemberException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByStudentId(Integer studentId);

    Boolean existsByStudentId(Integer studentId);

    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(MemberException.MEMBER_NOT_FOUND));
    }

    default Member getMemberByStudentId(Integer studentId) {
        return findByStudentId(studentId)
                .orElseThrow(() -> new ApplicationException(MemberException.MEMBER_NOT_FOUND));
    }
}
