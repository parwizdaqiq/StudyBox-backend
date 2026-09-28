package com.studybox.backend.controller;

import com.studybox.backend.dto.AssignmentResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.AssignmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<AssignmentResponse> list(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status) {
        return assignmentService.listForStudent(user.studentId(), courseId, status);
    }
}
