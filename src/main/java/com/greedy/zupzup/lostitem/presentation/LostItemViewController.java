package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.lostitem.application.LostItemViewService;
import com.greedy.zupzup.lostitem.application.MyPledgedLostItemService;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemViewController implements LostItemViewControllerDocs{

    private final LostItemViewService lostItemViewService;
    private final MyPledgedLostItemService myPledgedLostItemService;

    /** 목록 */
    @GetMapping
    public ResponseEntity<LostItemListResponse> list(@Valid LostItemListRequest query) {
        Page<LostItemListCommand> page = lostItemViewService.getLostItems(
                query.categoryId(), query.schoolAreaId(), query.safePage(), query.safeLimit()
        );

        List<Long> ids = page.getContent().stream().map(LostItemListCommand::id).toList();
        Map<Long, String> repImageMap = lostItemViewService.getRepresentativeImageMapByItemIds(ids);

        return ResponseEntity.ok(LostItemListResponse.of(page, repImageMap));
    }

    /** 단건 */
    @GetMapping("/{lostItemId}")
    public ResponseEntity<LostItemResponse> getBasic(@PathVariable Long lostItemId) {
        LostItemSimpleViewCommand command = lostItemViewService.getSimpleView(lostItemId);
        return ResponseEntity.ok(LostItemResponse.from(command));
    }

    /** 내가 서약한 분실물 목록 조회 */
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
