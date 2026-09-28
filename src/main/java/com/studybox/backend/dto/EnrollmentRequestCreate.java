package com.studybox.backend.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequestCreate(
        @NotNull Long catalogCourseId
) {
}
