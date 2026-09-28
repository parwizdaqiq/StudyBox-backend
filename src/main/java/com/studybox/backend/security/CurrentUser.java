package com.studybox.backend.security;

/**
 * Lightweight principal placed on the SecurityContext for an authenticated request.
 * {@code studentId} is the JWT subject — for a student token it's their {@code Student.id},
 * for an admin token it's the {@code AdminAccount.id} (kept under the same name so the many
 * existing student-facing controllers didn't need to change). {@code role} is "STUDENT" or
 * "ADMIN".
 */
public record CurrentUser(Long studentId, String email, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
