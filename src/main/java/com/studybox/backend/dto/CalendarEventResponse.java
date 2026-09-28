package com.studybox.backend.dto;

public record CalendarEventResponse(
        String date,
        String time,
        String title,
        String category,
        Long courseId,
        String courseTitle,
        String colorHex
) {
}
