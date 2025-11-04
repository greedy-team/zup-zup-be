package com.greedy.zupzup.admin.presentation;

import com.greedy.zupzup.category.application.CategoryService;
import com.greedy.zupzup.schoolarea.application.SchoolAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/cache")
public class AdminCacheController implements AdminCacheControllerDocs {

    private final SchoolAreaService schoolAreaService;
    private final CategoryService categoryService;

    @PostMapping("/school-areas/refresh")
    public ResponseEntity<Void> refreshSchoolAreaCache() {
        schoolAreaService.evictAllSchoolAreasCache();
        schoolAreaService.findAllAreas();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories/refresh")
    public ResponseEntity<Void> refreshCategoryCache() {
        categoryService.evictAllCategoryCaches();
        categoryService.getAll();
        return ResponseEntity.noContent().build();
    }

}
