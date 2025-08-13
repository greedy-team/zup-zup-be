package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemSummaryService;
import com.greedy.zupzup.lostitem.application.dto.LostItemSummaryCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemSummaryController {

    private final LostItemSummaryService service;

    @GetMapping("/summary")
    public ResponseEntity<LostItemSummaryResponse> getSummary(
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        List<LostItemSummaryCommand> result = service.getSummary(categoryId);

        List<LostItemSummaryResponse.AreaSummary> areas = result.stream()
                .map(dto -> new LostItemSummaryResponse.AreaSummary(
                        dto.schoolAreaId(),
                        dto.schoolAreaName(),
                        dto.lostCount()
                ))
                .toList();

        return ResponseEntity.ok(new LostItemSummaryResponse(areas));
    }
}
