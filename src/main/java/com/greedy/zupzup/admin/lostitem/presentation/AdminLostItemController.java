package com.greedy.zupzup.admin.lostitem.presentation;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/lost-items")
public class AdminLostItemController {

    private final AdminLostItemService adminLostItemService;

    @PostMapping("/approve")
    public ResponseEntity<ApproveLostItemsResponse> approveBulk(
            @Valid @RequestBody ApproveLostItemsRequest request) {
        ApproveLostItemsResponse result = adminLostItemService.approveBulk(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reject")
    public ResponseEntity<RejectLostItemsResponse> rejectBulk(
            @Valid @RequestBody RejectLostItemsRequest request) {
        RejectLostItemsResponse result = adminLostItemService.rejectBulk(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<AdminPendingLostItemListResponse> listPending(
            @Valid LostItemListRequest query) {
        AdminPendingLostItemListResponse response =
                adminLostItemService.getPendingLostItems(query.safePage(), query.safeLimit());

        return ResponseEntity.ok(response);
    }
}
