package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemRegisterService;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemRegisterService lostItemRegisterService;

    @PostMapping
    public ResponseEntity<LostItemRegisterResponse> create(@RequestPart("images") List<MultipartFile> images,
                                                           @RequestPart("lostItemRegisterRequest") LostItemRegisterRequest lostItemRegisterRequest) {
        LostItem lostItem = lostItemRegisterService.registLostItem(lostItemRegisterRequest.toCommand(images));
        return ResponseEntity
                .created(URI.create("/api/lost-items/"))
                .body(new LostItemRegisterResponse(lostItem.getId(), "분실물 등록에 성공했습니다."));
    }
}
