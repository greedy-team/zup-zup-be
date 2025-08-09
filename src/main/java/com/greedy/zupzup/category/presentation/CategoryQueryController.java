package com.greedy.zupzup.category.presentation;

import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.application.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryQueryController {

    private final CategoryQueryService categoryQueryService;

    @GetMapping
    public ResponseEntity<CategoriesResponse> categories() {
        return ResponseEntity.ok(categoryQueryService.getAll());
    }
}
