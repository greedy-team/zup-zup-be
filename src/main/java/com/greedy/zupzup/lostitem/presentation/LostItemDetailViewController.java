package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.application.LostItemDetailViewService;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewResult;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDepositAreaResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemDetailViewResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemDetailViewController implements LostItemDetailViewControllerDocs {

    private final LostItemDetailViewService service;

    @Override
    @GetMapping("/{lostItemId}/detail")
    public ResponseEntity<LostItemDetailViewResponse> getDetail(
            @PathVariable Long lostItemId,
            @MemberAuth LoginMember loginMember
    ) {
        LostItemDetailViewResult result = service.getDetail(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(LostItemDetailViewResponse.from(result));
    }

    /**
     * 퀴즈 후 상세 정보와 사진 조회
     */
    @Override
    @GetMapping("/{lostItemId}/image")
    public ResponseEntity<LostItemImageResponse> getImagesAfterQuiz(
            @PathVariable Long lostItemId,
            @MemberAuth LoginMember loginMember
    ) {
        LostItemDetailViewResult result =
                service.getImagesAfterQuiz(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(LostItemImageResponse.from(result));
    }

    /**
     * 서약 후 보관 장소 공개
     */
    @Override
    @GetMapping("/{lostItemId}/deposit-area")
    public ResponseEntity<LostItemDepositAreaResponse> getDepositArea(
            @PathVariable Long lostItemId,
            @MemberAuth LoginMember loginMember
    ) {
        String depositArea = service.getDepositArea(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(LostItemDepositAreaResponse.from(depositArea));
    }
}
