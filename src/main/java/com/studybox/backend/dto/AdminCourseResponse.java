package com.studybox.backend.dto;

import java.util.List;

public record AdminCourseResponse(
        Long id,
        String code,
        String title,
        int credits,
        String term,
        String colorHex,
        String icon,
        List<String> instructorNames,
        int enrolledCount,
        String description
) {
}
