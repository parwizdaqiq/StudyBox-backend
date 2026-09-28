package com.studybox.backend.dto;

public record CatalogCourseResponse(
        Long id,
        String code,
        String title,
        int credits,
        String term,
        String category,
        String description
) {
}
