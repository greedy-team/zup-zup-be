package com.greedy.zupzup.category.repository;

import com.greedy.zupzup.category.domain.Category;

import java.util.List;
import java.util.Optional;

import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.global.exception.ApplicationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByIdAsc();

    @Query("""
               select distinct c
               from Category c
               left join fetch c.features f
               where c.id = :id
            """)
    Optional<Category> findWithFeaturesById(@Param("id") Long id);

    default Category getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));
    }

    default List<Category> getAllByIds(List<Long> ids) {
        List<Category> categories = findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new ApplicationException(CategoryException.CATEGORY_NOT_FOUND);
        }
        return categories;
    }
}
