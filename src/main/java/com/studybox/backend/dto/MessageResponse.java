package com.studybox.backend.dto;

public record MessageResponse(
        Long id,
        String senderName,
        boolean fromStudent,
        String body,
        String sentAt
) {
}
