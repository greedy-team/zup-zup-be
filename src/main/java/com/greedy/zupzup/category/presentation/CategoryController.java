package com.greedy.zupzup.category.presentation;

import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.application.CategoryService;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController implements CategoryControllerDocs {

    private final CategoryService categoryService;

    @Override
    @GetMapping
    public ResponseEntity<CategoriesResponse> categories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @Override
    @GetMapping("/{categoryId}/features")
    public ResponseEntity<CategoryFeaturesResponse> getCategoryFeatures(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getCategoryFeatures(categoryId));
    }
}
