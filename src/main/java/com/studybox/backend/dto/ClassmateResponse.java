package com.studybox.backend.dto;

public record ClassmateResponse(
        Long id,
        String fullName,
        String initials,
        String programme,
        Long courseId,
        String courseTitle
) {
}
