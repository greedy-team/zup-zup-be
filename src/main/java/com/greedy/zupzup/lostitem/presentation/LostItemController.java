package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemRegisterService;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemController implements LostItemControllerDocs {

    private final LostItemRegisterService lostItemRegisterService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LostItemRegisterResponse> create(@RequestPart("images") List<MultipartFile> images,
                                                           @Valid @RequestPart("lostItemRegisterRequest") LostItemRegisterRequest lostItemRegisterRequest) {
        LostItem lostItem = lostItemRegisterService.registLostItem(lostItemRegisterRequest.toCommand(images));
        return ResponseEntity
                .created(URI.create("/api/lost-items/" + lostItem.getId()))
                .body(new LostItemRegisterResponse(lostItem.getId(), "분실물 등록에 성공했습니다."));
    }

}
