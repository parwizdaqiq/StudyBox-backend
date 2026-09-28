package com.studybox.backend.controller;

import com.studybox.backend.dto.StudentProfileResponse;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.StudentRepository;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;

    public ProfileController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public StudentProfileResponse profile(@AuthenticationPrincipal CurrentUser user) {
        var student = studentRepository.findById(user.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        return AuthService.toProfile(student);
    }
}
