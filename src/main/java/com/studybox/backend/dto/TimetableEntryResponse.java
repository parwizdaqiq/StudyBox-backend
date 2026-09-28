package com.studybox.backend.dto;

public record TimetableEntryResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        String colorHex,
        String dayOfWeek,
        String startTime,
        String endTime,
        String room,
        String weekType,
        String sessionLabel
) {
}
