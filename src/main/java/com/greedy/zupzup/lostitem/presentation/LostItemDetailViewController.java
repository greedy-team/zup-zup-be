package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemDetailViewService;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDetailViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemDetailViewController {

    private final LostItemDetailViewService service;

    @GetMapping("/{lostItemId}/detail")
    public ResponseEntity<LostItemDetailViewResponse> getDetail(@PathVariable Long lostItemId,
                                                                @RequestParam Long memberId) {
        LostItemDetailViewCommand cmd = service.getDetail(lostItemId, memberId);
        return ResponseEntity.ok(LostItemDetailViewResponse.from(cmd));
    }
}
