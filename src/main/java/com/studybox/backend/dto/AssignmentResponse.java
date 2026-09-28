package com.studybox.backend.dto;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        String colorHex,
        String title,
        String type,
        String categoryLabel,
        String availableFrom,
        String dueAt,
        double pointsPossible,
        Double scoreEarned,
        String status
) {
}
