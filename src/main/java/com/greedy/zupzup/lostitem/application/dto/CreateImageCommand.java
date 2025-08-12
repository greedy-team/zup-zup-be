package com.greedy.zupzup.lostitem.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public record CreateImageCommand(
        MultipartFile imageFile,
        int order
) {
}
