package com.greedy.zupzup.admin.lostitem.presentation;

import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.auth.presentation.annotation.AdminAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginAdmin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @AdminAuth LoginAdmin admin,
            @Valid @RequestBody ApproveLostItemsRequest request
    ) {
        ApproveLostItemsResponse result = adminLostItemService.approveBulk(request);
        return ResponseEntity.ok(result);
    }
}
