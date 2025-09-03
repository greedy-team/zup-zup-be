package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItemImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostItemImageRepository extends JpaRepository<LostItemImage, Long> {

    @Query("""
        select i.lostItem.id as lostItemId,
               coalesce(i.imageKey, '') as imageUrl
          from LostItemImage i
         where i.lostItem.id in :ids
           and i.imageOrder = (
                select min(i2.imageOrder)
                  from LostItemImage i2
                 where i2.lostItem.id = i.lostItem.id
           )
        """)
    List<RepresentativeImageProjection> findRepresentativeImages(@Param("ids") Collection<Long> ids);

    @Query("""
                select i.imageKey
                  from LostItemImage i
                 where i.lostItem.id = :lostItemId
                 order by i.imageOrder asc
            """)
    List<String> findImageUrlsByLostItemId(@Param("lostItemId") Long lostItemId);
}
