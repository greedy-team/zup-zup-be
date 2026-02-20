package com.greedy.zupzup.alert.repository;

import com.greedy.zupzup.alert.domain.KeywordAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordAlertRepository extends JpaRepository<KeywordAlert, Long> {

    List<KeywordAlert> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
