package com.studybox.backend.controller;

import com.studybox.backend.dto.InstructorResponse;
import com.studybox.backend.service.EducatorDirectoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/educators")
public class EducatorController {

    private final EducatorDirectoryService educatorDirectoryService;

    public EducatorController(EducatorDirectoryService educatorDirectoryService) {
        this.educatorDirectoryService = educatorDirectoryService;
    }

    @GetMapping
    public List<InstructorResponse> all() {
        return educatorDirectoryService.all();
    }
}
