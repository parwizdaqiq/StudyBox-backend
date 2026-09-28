package com.studybox.backend.dto;

public record SessionResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        String colorHex,
        String sessionDate,
        String sessionLabel,
        String status,
        boolean hasFiles
) {
}
