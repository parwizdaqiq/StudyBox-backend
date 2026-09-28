package com.studybox.backend.dto;

public record AdminGradeResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseCode,
        String categoryName,
        String itemName,
        double maxScore,
        Double scoreEarned,
        boolean posted,
        /** The student's recomputed weighted total for this course, now that this grade posted. */
        Double courseTotalPercent
) {
}
