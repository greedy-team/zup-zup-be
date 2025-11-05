package com.greedy.zupzup.admin.lostitem.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RejectLostItemsRequest(
        @NotEmpty(message = "삭제할 분실물 ID 목록은 비어 있을 수 없습니다.")
        @Size(min = 1, message = "삭제할 분실물 ID는 최소 1개 이상이어야 합니다.")
        List<Long> lostItemIds

) {}
