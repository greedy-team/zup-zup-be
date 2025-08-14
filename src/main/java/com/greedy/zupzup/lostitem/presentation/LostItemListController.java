package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemListService;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListQuery;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemListController {

    private final LostItemListService lostItemListService;

    @GetMapping
    public ResponseEntity<LostItemListResponse> list(@Valid LostItemListQuery query) {
        Page<LostItemListCommand> page = lostItemListService.getLostItems(
                query.categoryId(), query.schoolAreaId(), query.safePage(), query.safeLimit()
        );

        List<Long> ids = page.getContent().stream().map(LostItemListCommand::id).toList();
        Map<Long, String> repImageMap = lostItemListService.getRepresentativeImageMapByItemIds(ids);

        return ResponseEntity.ok(LostItemListResponse.of(page, repImageMap));
    }
}
