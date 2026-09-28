package com.studybox.backend.controller;

import com.studybox.backend.dto.DiscussionPostCreate;
import com.studybox.backend.dto.DiscussionThreadCreate;
import com.studybox.backend.dto.DiscussionThreadDetailResponse;
import com.studybox.backend.dto.DiscussionThreadSummaryResponse;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.StudentRepository;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.DiscussionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;
    private final StudentRepository studentRepository;

    public DiscussionController(DiscussionService discussionService, StudentRepository studentRepository) {
        this.discussionService = discussionService;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<DiscussionThreadSummaryResponse> list(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) Long courseId) {
        return discussionService.list(user.studentId(), courseId);
    }

    @GetMapping("/{id}")
    public DiscussionThreadDetailResponse detail(@PathVariable Long id) {
        return discussionService.detail(id);
    }

    @PostMapping("/{id}/posts")
    public DiscussionThreadDetailResponse addPost(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @Valid @RequestBody DiscussionPostCreate request) {
        String authorName = studentRepository.findById(user.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"))
                .getFullName();
        return discussionService.addPost(user.studentId(), id, request.body(), authorName);
    }

    @PostMapping
    public DiscussionThreadDetailResponse createThread(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody DiscussionThreadCreate request) {
        String authorName = studentRepository.findById(user.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"))
                .getFullName();
        return discussionService.createThread(user.studentId(), request.courseId(), request.title(), request.body(), authorName);
    }
}
