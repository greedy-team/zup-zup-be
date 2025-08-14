package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemListService;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListQuery;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.PageInfoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
        Page<LostItemListCommand> result = lostItemListService.getLostItems(
                query.categoryId(),
                query.schoolAreaId(),
                query.safePage(),
                query.safeLimit()
        );

        List<LostItemResponse> items = result.getContent().stream()
                .map(LostItemResponse::from)
                .toList();

        PageInfoResponse pageInfo = PageInfoResponse.of(
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasPrevious(),
                result.hasNext()
        );

        LostItemListResponse body = new LostItemListResponse(result.getNumberOfElements(), items, pageInfo);
        return ResponseEntity.ok(body);
    }
}
