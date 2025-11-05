package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItemImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostItemImageRepository extends JpaRepository<LostItemImage, Long> {

    @Query("""
             select i.lostItem.id as lostItemId, i.imageKey as imageUrl
                        from LostItemImage i
                        where i.imageOrder = 0 and i.lostItem.id in :ids
            """)
    List<RepresentativeImageProjection> findRepresentativeImages(@Param("ids") Collection<Long> ids);

    @Query("""
                select i.imageKey
                  from LostItemImage i
                 where i.lostItem.id = :lostItemId
                 order by i.imageOrder asc
            """)
    List<String> findImageUrlsByLostItemId(@Param("lostItemId") Long lostItemId);

    @Query("SELECT i.imageKey FROM LostItemImage i WHERE i.lostItem.id IN :lostItemIds")
    List<String> findImageKeysByLostItemIds(@Param("lostItemIds") List<Long> lostItemIds);

    @Modifying
    @Query("DELETE FROM LostItemImage i WHERE i.lostItem.id IN :lostItemIds")
    void deleteByLostItemIds(@Param("lostItemIds") List<Long> lostItemIds);

    @Query("""
                select i
                from LostItemImage i
                where i.lostItem.id in :lostItemIds
                order by i.imageOrder asc
            """)
    List<LostItemImage> findImagesForItems(@Param("lostItemIds") List<Long> lostItemIds);

}
