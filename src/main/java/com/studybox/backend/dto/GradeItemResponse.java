package com.studybox.backend.dto;

public record GradeItemResponse(
        Long id,
        String name,
        double maxScore,
        Double scoreEarned,
        boolean posted,
        String dueAt
) {
}
