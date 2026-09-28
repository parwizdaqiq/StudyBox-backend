package com.studybox.backend.dto;

public record AdminEnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseCode,
        String courseTitle,
        int presences,
        int lates,
        int authorizedAbsences,
        int unauthorizedAbsences
) {
}
