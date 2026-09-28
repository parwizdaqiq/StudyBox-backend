package com.studybox.backend.dto;

public record EnrollmentRequestResponse(
        Long id,
        Long catalogCourseId,
        String courseCode,
        String courseTitle,
        String requestedAt,
        String status
) {
}
