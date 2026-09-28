package com.studybox.backend.controller;

import com.studybox.backend.dto.ClassmateResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.ClassmatesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classmates")
public class ClassmatesController {

    private final ClassmatesService classmatesService;

    public ClassmatesController(ClassmatesService classmatesService) {
        this.classmatesService = classmatesService;
    }

    @GetMapping
    public List<ClassmateResponse> list(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) Long courseId) {
        return classmatesService.forStudent(user.studentId(), courseId);
    }
}
