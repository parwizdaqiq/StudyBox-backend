package com.studybox.backend.dto;

public record StudentProfileResponse(
        Long id,
        String studentNumber,
        String fullName,
        String email,
        String programme,
        String photoUrl,
        String firstLoginDate,
        String lastLoginAt,
        int totalLogins,
        int loginFrequencyPercent
) {
}
