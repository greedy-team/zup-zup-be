package com.greedy.zupzup.lostitem.application.dto;

import org.springframework.web.multipart.MultipartFile;

public record CreateImageCommand(
        MultipartFile imageFile,
        int order
) {
}
