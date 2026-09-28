package com.studybox.backend.controller;

import com.studybox.backend.dto.AdminAuthResponse;
import com.studybox.backend.dto.LoginRequest;
import com.studybox.backend.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Staff/Super Admin sign-in — deliberately separate from {@code /api/auth} so a student's
 *  credentials and an admin's credentials are never checked against the same table. */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public AdminAuthResponse login(@Valid @RequestBody LoginRequest request) {
        return adminAuthService.login(request);
    }
}
