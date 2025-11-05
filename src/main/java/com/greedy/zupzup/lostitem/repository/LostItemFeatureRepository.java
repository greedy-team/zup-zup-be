package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostItemFeatureRepository extends JpaRepository<LostItemFeature, Long> {

    @Query("""
            select lif from LostItemFeature lif
            join fetch lif.feature f
            join fetch f.options
            where lif.lostItem.id = :lostItemId
            """)
    List<LostItemFeature> findWithFeatureAndOptionsByLostItemId(Long lostItemId);

    List<LostItemFeature> findByLostItemId(Long lostItemId);

    @Modifying
    @Query("DELETE FROM LostItemFeature lif WHERE lif.lostItem.id IN :lostItemIds")
    void deleteByLostItemIds(@Param("lostItemIds") List<Long> lostItemIds);

    @Query("""
    select lif from LostItemFeature lif
    join fetch lif.lostItem li
    join fetch lif.feature f
    join fetch lif.selectedOption fo
    where lif.lostItem.id in :lostItemIds
    """)
    List<LostItemFeature> findFeaturesForLostItems(@Param("lostItemIds") List<Long> lostItemIds);
}
