package com.studybox.backend.dto;

import java.util.List;

public record GradeCourseResponse(
        Long courseId,
        String courseCode,
        String courseTitle,
        String colorHex,
        List<GradeCategoryResponse> categories,
        Double totalPercent
) {
}
