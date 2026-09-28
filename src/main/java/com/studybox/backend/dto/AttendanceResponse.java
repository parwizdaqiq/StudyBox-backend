package com.studybox.backend.dto;

public record AttendanceResponse(
        int unauthorizedAbsences,
        int authorizedAbsences,
        int lates,
        int presences
) {
}
