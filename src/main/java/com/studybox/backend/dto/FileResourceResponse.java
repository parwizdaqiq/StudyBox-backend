package com.studybox.backend.dto;

public record FileResourceResponse(
        Long id,
        String fileName,
        String fileType,
        String sizeLabel,
        String category,
        Long courseId,
        String courseTitle,
        String uploadedAt
) {
}
