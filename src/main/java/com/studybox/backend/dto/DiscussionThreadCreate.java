package com.studybox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A student starting a new discussion thread in one of their courses. */
public record DiscussionThreadCreate(
        @NotNull Long courseId,
        @NotBlank String title,
        @NotBlank String body
) {
}
