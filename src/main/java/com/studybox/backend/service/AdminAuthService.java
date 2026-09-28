package com.studybox.backend.service;

import com.studybox.backend.domain.AdminAccount;
import com.studybox.backend.dto.AdminAuthResponse;
import com.studybox.backend.dto.AdminProfileResponse;
import com.studybox.backend.dto.LoginRequest;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.repository.AdminAccountRepository;
import com.studybox.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminAccountRepository adminAccountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AdminAuthResponse login(LoginRequest request) {
        AdminAccount admin = adminAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(admin.getId(), admin.getEmail(), admin.getRole());
        return new AdminAuthResponse(token, toProfile(admin));
    }

    public static AdminProfileResponse toProfile(AdminAccount admin) {
        return new AdminProfileResponse(admin.getId(), admin.getFullName(), admin.getEmail(), admin.getRole());
    }
}
