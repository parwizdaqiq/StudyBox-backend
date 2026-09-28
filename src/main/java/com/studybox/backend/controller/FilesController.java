package com.studybox.backend.controller;

import com.studybox.backend.dto.FileResourceResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.FilesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FilesController {

    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    @GetMapping
    public List<FileResourceResponse> list(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) Long courseId) {
        return filesService.list(user.studentId(), courseId);
    }
}
