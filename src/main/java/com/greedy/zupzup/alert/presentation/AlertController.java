package com.greedy.zupzup.alert.presentation;

import com.greedy.zupzup.alert.application.AlertService;
import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.presentation.dto.SubscriptionResponse;
import com.greedy.zupzup.alert.presentation.dto.SubscriptionUpdateRequest;
import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController implements AlertControllerDocs {

    private final AlertService alertService;

    @Override
    @GetMapping("/subscriptions")
    public ResponseEntity<SubscriptionResponse> getSubscriptions(
            @MemberAuth LoginMember loginMember
    ) {
        SubscriptionResult result = alertService.getSubscriptions(loginMember.memberId());
        return ResponseEntity.ok(SubscriptionResponse.from(result));
    }

    @Override
    @PutMapping("/subscriptions")
    public ResponseEntity<Void> updateSubscriptions(
            @MemberAuth LoginMember loginMember,
            @RequestBody @Valid SubscriptionUpdateRequest request
    ) {
        SubscriptionUpdateCommand command = SubscriptionUpdateCommand.of(loginMember.memberId(), request.categoryIds());
        alertService.updateSubscriptions(command);
        return ResponseEntity.ok().build();
    }
}
