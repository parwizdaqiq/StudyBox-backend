package com.studybox.backend.dto;

import java.util.List;

public record InstructorResponse(
        Long id,
        String fullName,
        String title,
        String email,
        String avatarInitials,
        List<String> courses
) {
}
