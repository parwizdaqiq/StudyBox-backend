package com.studybox.backend.dto;

import java.util.List;

public record AdminStudentResponse(
        Long id,
        String studentNumber,
        String fullName,
        String email,
        String programme,
        String createdAt,
        String lastLoginAt,
        int totalLogins,
        List<AdminCourseRefResponse> courses
) {
}
