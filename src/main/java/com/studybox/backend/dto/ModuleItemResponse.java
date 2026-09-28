package com.studybox.backend.dto;

public record ModuleItemResponse(
        Long id,
        String type,
        String title,
        String dueAt,
        Double points
) {
}
