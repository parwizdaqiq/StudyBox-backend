package com.studybox.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageReplyRequest(
        @NotBlank String body
) {
}
