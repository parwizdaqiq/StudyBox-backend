package com.studybox.backend.dto;

/** Small course reference used inside {@link AdminStudentResponse} — avoids re-fetching the
 *  full course payload just to show a student's enrolled course chips. */
public record AdminCourseRefResponse(
        Long enrollmentId,
        Long courseId,
        String code,
        String title,
        String colorHex
) {
}
