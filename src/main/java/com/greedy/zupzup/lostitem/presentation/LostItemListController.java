package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemListService;
import com.greedy.zupzup.lostitem.application.dto.LostItemListDto;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.PageInfoResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemListController {

    private final LostItemListService lostItemListService;

    @GetMapping
    public ResponseEntity<LostItemListResponse> list(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "schoolAreaId", required = false) Long schoolAreaId,
            @RequestParam(name = "page", required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "limit", required = false, defaultValue = "20") @Min(1) @Max(50) Integer limit
    ) {
        {
            Page<LostItemListDto> result = lostItemListService.getLostItems(categoryId, schoolAreaId, page, limit);

            if (result == null)
                result = org.springframework.data.domain.Page.empty();

            List<LostItemResponse> items = result.getContent()
                    .stream()
                    .map(LostItemResponse::from)
                    .collect(Collectors.toList());

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
}
