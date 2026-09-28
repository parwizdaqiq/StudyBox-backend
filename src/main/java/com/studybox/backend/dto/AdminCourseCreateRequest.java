package com.studybox.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminCourseCreateRequest(
        @NotBlank String code,
        @NotBlank String title,
        int credits,
        String term,
        String colorHex,
        String icon,
        String description
) {
}
