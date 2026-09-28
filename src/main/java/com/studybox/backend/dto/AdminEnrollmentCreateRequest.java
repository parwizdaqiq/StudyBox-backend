package com.studybox.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AdminEnrollmentCreateRequest(
        @NotNull Long studentId,
        @NotNull Long courseId
) {
}
