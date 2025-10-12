package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.application.MyPledgedLostItemService;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class MyPledgedLostItemController implements MyPledgedLostItemControllerDocs {

    private final MyPledgedLostItemService myPledgedLostItemService;

    @GetMapping("/pledged")
    public ResponseEntity<LostItemListResponse> getMyPledgedLostItems(
            @MemberAuth LoginMember loginMember,
            @Valid MyPledgedListRequest query
    ) {
        LostItemListResponse response = myPledgedLostItemService.getMyPledgedLostItems(
                loginMember.memberId(),
                query.safePage(),
                query.safeLimit()
        );
        return ResponseEntity.ok(response);
    }
}
