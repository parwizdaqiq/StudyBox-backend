package com.studybox.backend.controller;

import com.studybox.backend.dto.GradeCourseResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.GradeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public List<GradeCourseResponse> list(@AuthenticationPrincipal CurrentUser user) {
        return gradeService.listForStudent(user.studentId());
    }
}
