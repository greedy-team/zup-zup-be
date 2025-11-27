package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemPledgeService;
import com.greedy.zupzup.lostitem.presentation.dto.CancelPledgeResponse;
import com.greedy.zupzup.lostitem.presentation.dto.FoundCompleteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemPledgeController {

    private final LostItemPledgeService lostItemPledgeService;

    @PostMapping("/{id}/pledge/cancel")
    public ResponseEntity<CancelPledgeResponse> cancelPledge(
            @PathVariable Long id,
            @RequestAttribute("loginMemberId") Long memberId
    ) {
        CancelPledgeResponse response = lostItemPledgeService.cancelPledge(memberId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/found")
    public ResponseEntity<FoundCompleteResponse> completeFound(
            @PathVariable Long id,
            @RequestAttribute("loginMemberId") Long memberId
    ) {
        FoundCompleteResponse response = lostItemPledgeService.completeFound(memberId, id);
        return ResponseEntity.ok(response);
    }
}
