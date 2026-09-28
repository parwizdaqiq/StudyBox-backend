package com.studybox.backend.dto;

import java.util.List;

public record DashboardResponse(
        StudentProfileResponse student,
        DashboardStatsResponse stats,
        List<TimetableEntryResponse> today,
        List<AssignmentResponse> upcomingAssignments,
        List<AnnouncementResponse> announcements,
        List<QuickActionResponse> quickActions
) {
}
