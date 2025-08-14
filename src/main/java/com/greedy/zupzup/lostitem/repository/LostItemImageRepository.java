package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItemImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostItemImageRepository extends JpaRepository<LostItemImage, Long> {

    interface RepImageProjection {
        Long getLostItemId();
        String getImageUrl();
    }

    @Query("""
        select i.lostItem.id as lostItemId, i.imageKey as imageUrl
        from LostItemImage i
        where i.imageOrder = 0 and i.lostItem.id in :ids
        """)
    List<RepImageProjection> findRepresentativeImages(@Param("ids") Collection<Long> ids);
}
