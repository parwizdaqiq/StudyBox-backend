package com.studybox.backend.controller;

import com.studybox.backend.dto.CourseDetailResponse;
import com.studybox.backend.dto.CourseSummaryResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseSummaryResponse> myCourses(@AuthenticationPrincipal CurrentUser user) {
        return courseService.listMyCourses(user.studentId());
    }

    @GetMapping("/{courseId}")
    public CourseDetailResponse courseDetail(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long courseId) {
        return courseService.getCourseDetail(user.studentId(), courseId);
    }
}
