package com.greedy.zupzup.pledge.presentation;

import com.greedy.zupzup.pledge.application.PledgeService;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.pledge.presentation.dto.PledgeResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lost-items/{lostItemId}/pledge")
@RequiredArgsConstructor
public class PledgeController {

    private final PledgeService pledgeService;

    @PostMapping
    public ResponseEntity<PledgeResponse> createPledge(
            @PathVariable Long lostItemId,
            Long memberId
    ) {
        Pledge pledge = pledgeService.createPledge(lostItemId, memberId);
        return ResponseEntity
                .created(URI.create("/api/pledges/" + pledge.getId()))
                .body(PledgeResponse.from(pledge));
    }
}
