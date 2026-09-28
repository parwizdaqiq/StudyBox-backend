package com.studybox.backend.controller;

import com.studybox.backend.dto.TimetableEntryResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.TimetableService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public List<TimetableEntryResponse> week(@AuthenticationPrincipal CurrentUser user) {
        return timetableService.weekFor(user.studentId());
    }

    @GetMapping("/today")
    public List<TimetableEntryResponse> today(@AuthenticationPrincipal CurrentUser user) {
        return timetableService.today(user.studentId());
    }
}
