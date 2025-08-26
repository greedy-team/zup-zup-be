package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.application.LostItemDetailViewService;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
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
        LostItemDetailViewCommand command = service.getDetail(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(LostItemDetailViewResponse.from(command));
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
        LostItemDetailViewCommand command =
                service.getImagesAfterQuiz(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(LostItemImageResponse.from(command));
    }
}
