package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LostItemFeatureRepository extends JpaRepository<LostItemFeature, Long> {

    @Query("""
            select lif from LostItemFeature lif
            join fetch lif.feature f
            join fetch f.options
            where lif.lostItem.id = :lostItemId
            """)
    List<LostItemFeature> findWithFeatureAndOptionsByLostItemId(Long lostItemId);
}
