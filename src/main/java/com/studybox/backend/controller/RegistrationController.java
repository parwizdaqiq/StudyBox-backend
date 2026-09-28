package com.studybox.backend.controller;

import com.studybox.backend.dto.CatalogCourseResponse;
import com.studybox.backend.dto.EnrollmentRequestCreate;
import com.studybox.backend.dto.EnrollmentRequestResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/catalog")
    public List<CatalogCourseResponse> catalog() {
        return registrationService.catalog();
    }

    @GetMapping("/requests")
    public List<EnrollmentRequestResponse> myRequests(@AuthenticationPrincipal CurrentUser user) {
        return registrationService.myRequests(user.studentId());
    }

    @PostMapping("/requests")
    public EnrollmentRequestResponse submit(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody EnrollmentRequestCreate request) {
        return registrationService.submitRequest(user.studentId(), request.catalogCourseId());
    }
}
