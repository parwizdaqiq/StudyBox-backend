package com.studybox.backend.dto;

public record AuthResponse(
        String token,
        StudentProfileResponse student
) {
}
