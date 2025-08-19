package com.greedy.zupzup.category.repository;

import com.greedy.zupzup.category.domain.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
}
