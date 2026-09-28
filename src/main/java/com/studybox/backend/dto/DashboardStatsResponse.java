package com.studybox.backend.dto;

public record DashboardStatsResponse(
        int activeCourses,
        int pendingAssignments,
        double attendanceRatePercent,
        Double averageGradePercent
) {
}
