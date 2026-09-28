package com.studybox.backend.dto;

import java.util.List;

public record GradeCategoryResponse(
        String name,
        double weightPercent,
        List<GradeItemResponse> items,
        Double categoryPercent
) {
}
