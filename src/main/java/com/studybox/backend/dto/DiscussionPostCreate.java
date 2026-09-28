package com.studybox.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DiscussionPostCreate(
        @NotBlank String body
) {
}
