package com.studybox.backend.controller;

import com.studybox.backend.dto.DashboardResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal CurrentUser user) {
        return dashboardService.build(user.studentId());
    }
}
