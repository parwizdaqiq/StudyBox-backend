package com.studybox.backend.dto;

public record AdminProfileResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
