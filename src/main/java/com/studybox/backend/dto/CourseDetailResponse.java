package com.studybox.backend.dto;

import java.util.List;

public record CourseDetailResponse(
        CourseSummaryResponse course,
        AttendanceResponse attendance,
        List<InstructorResponse> instructors,
        List<ModuleResponse> modules
) {
}
