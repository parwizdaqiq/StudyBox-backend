package com.studybox.backend.dto;

import java.util.List;

public record ModuleResponse(
        Long id,
        String weekLabel,
        String title,
        List<ModuleItemResponse> items
) {
}
