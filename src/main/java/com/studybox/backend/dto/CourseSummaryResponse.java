package com.studybox.backend.dto;

import java.util.List;

public record CourseSummaryResponse(
        Long id,
        String code,
        String title,
        int credits,
        String term,
        String colorHex,
        String icon,
        List<String> instructorNames
) {
}
