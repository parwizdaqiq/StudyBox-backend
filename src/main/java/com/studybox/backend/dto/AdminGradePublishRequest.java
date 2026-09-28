package com.studybox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminGradePublishRequest(
        @NotNull Long studentId,
        @NotNull Long courseId,
        /** Grade-category bucket, e.g. "Assessment 1", "Practice". Created if it doesn't exist yet. */
        @NotBlank String categoryName,
        /** Only used the first time this category is created for the course; ignored after that. */
        double categoryWeightPercent,
        @NotBlank String itemName,
        @Positive double maxScore,
        @NotNull Double scoreEarned
) {
}
