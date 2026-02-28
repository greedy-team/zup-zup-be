    package com.greedy.zupzup.admin.lostitem.presentation;

    import com.greedy.zupzup.admin.lostitem.application.AdminLostItemService;
    import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemResult;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
    import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemCommand;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemRequest;
    import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemResponse;
    import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
    import jakarta.validation.Valid;
    import java.util.List;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.PutMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestPart;
    import org.springframework.web.bind.annotation.RestController;
    import org.springframework.web.multipart.MultipartFile;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/admin/lost-items")
    public class AdminLostItemController implements AdminLostItemControllerDocs{

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

        @PutMapping(value = "/{lostItemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<UpdateLostItemResponse> updateLostItem(
                @PathVariable Long lostItemId,
                @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
                @RequestPart("updateRequest") @Valid UpdateLostItemRequest updateRequest
        ) {
            UpdateLostItemCommand command = UpdateLostItemCommand.of(lostItemId, updateRequest, newImages);
            UpdateLostItemResult result = adminLostItemService.updateLostItem(command);
            return ResponseEntity.ok(UpdateLostItemResponse.from(result.lostItemId()));
        }
    }
