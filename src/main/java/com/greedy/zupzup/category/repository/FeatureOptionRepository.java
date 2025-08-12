package com.greedy.zupzup.category.repository;

import com.greedy.zupzup.category.domain.FeatureOption;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeatureOptionRepository extends JpaRepository<FeatureOption, Long> {

    @Query("""
       select fo
       from FeatureOption fo
       join fetch fo.feature f
       where f.id in :featureIds
       """)
    List<FeatureOption> findByFeatureIds(@Param("featureIds") Collection<Long> featureIds);
}
