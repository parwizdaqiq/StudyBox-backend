package com.studybox.backend.dto;

public record AdminAuthResponse(
        String token,
        AdminProfileResponse admin
) {
}
