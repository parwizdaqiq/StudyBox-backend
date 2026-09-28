package com.studybox.backend.dto;

public record AdminOverviewResponse(
        int studentCount,
        int courseCount,
        int enrollmentCount,
        int publishedGradeCount
) {
}
