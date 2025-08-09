package com.greedy.zupzup.category.application;

import com.greedy.zupzup.category.application.dto.CategoryDto;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CategoriesResponse getAll() {
        var list = categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryDto::from)
                .toList();
        return new CategoriesResponse(list);
    }
}
