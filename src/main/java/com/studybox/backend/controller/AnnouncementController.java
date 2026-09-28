package com.studybox.backend.controller;

import com.studybox.backend.dto.AnnouncementResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Full announcement feed for the signed-in student (the dashboard only shows the latest few). */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final DashboardService dashboardService;

    public AnnouncementController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public List<AnnouncementResponse> list(@AuthenticationPrincipal CurrentUser user) {
        return dashboardService.announcementsForStudent(user.studentId());
    }
}
