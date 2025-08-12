package com.greedy.zupzup.category.repository;

import com.greedy.zupzup.category.domain.Category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNameAsc();

    @Query("""
               select distinct c
               from Category c
               left join fetch c.features f
               where c.id = :id
            """)
    Optional<Category> findWithFeaturesById(@Param("id") Long id);

}
