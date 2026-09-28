package com.studybox.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request/response shapes for the Super Admin's content-management endpoints (everything
 * beyond the original create-student / create-course / enroll / publish-grade set). Grouped in
 * one holder class to keep the dto package from exploding into dozens of tiny files.
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    // ---------------------------------------------------------------- students

    public record StudentUpdateRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            String programme) {
    }

    public record PasswordResetRequest(
            @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password) {
    }

    // ---------------------------------------------------------------- courses

    public record CourseInstructorsRequest(@NotNull List<Long> educatorIds) {
    }

    // -------------------------------------------------------------- educators

    public record EducatorRequest(
            @NotBlank String fullName,
            String title,
            @NotBlank @Email String email) {
    }

    public record EducatorResponse(
            Long id,
            String fullName,
            String title,
            String email,
            String avatarInitials,
            List<Long> courseIds,
            List<String> courseCodes) {
    }

    // ------------------------------------------------------------- attendance

    public record SessionCreateRequest(
            @NotNull Long studentId,
            @NotNull Long courseId,
            @NotBlank String sessionDate, // yyyy-MM-dd
            String sessionLabel,
            @NotBlank String status) { // PRESENT | LATE | AUTHORIZED_ABSENCE | UNAUTHORIZED_ABSENCE
    }

    public record SessionResponse(
            Long id,
            Long studentId,
            String studentName,
            Long courseId,
            String courseCode,
            String sessionDate,
            String sessionLabel,
            String status) {
    }

    // ----------------------------------------------------------------- grades

    public record GradeUpdateRequest(
            @NotBlank String itemName,
            @PositiveOrZero double maxScore,
            Double scoreEarned,
            boolean posted) {
    }

    public record GradeCategoryResponse(Long id, Long courseId, String name, double weightPercent) {
    }

    public record GradeCategoryRequest(@NotBlank String name, @PositiveOrZero double weightPercent) {
    }

    // ------------------------------------------------------------ assignments

    public record AssignmentRequest(
            @NotNull Long courseId,
            @NotBlank String title,
            @NotBlank String type, // QUIZ | LAB | ASSIGNMENT | ASSESSMENT
            String categoryLabel,
            String availableFrom, // yyyy-MM-ddTHH:mm (optional)
            String dueAt, // yyyy-MM-ddTHH:mm (optional)
            @PositiveOrZero double pointsPossible) {
    }

    // ---------------------------------------------------------- announcements

    public record AnnouncementRequest(
            Long courseId, // null = everyone
            @NotBlank String title,
            @NotBlank String body) {
    }

    public record AnnouncementResponse(
            Long id,
            Long courseId,
            String courseTitle,
            String title,
            String body,
            String postedAt,
            String postedBy) {
    }

    // -------------------------------------------------------------- timetable

    public record TimetableRequest(
            @NotNull Long courseId,
            @NotBlank String dayOfWeek, // MONDAY … SUNDAY
            @NotBlank String startTime, // HH:mm
            @NotBlank String endTime, // HH:mm
            String room,
            String weekType, // ALL | ODD | EVEN
            String sessionLabel) {
    }

    // ---------------------------------------------------------------- modules

    public record ModuleRequest(@NotNull Long courseId, String weekLabel, @NotBlank String title) {
    }

    public record ModuleItemRequest(
            @NotBlank String type, // VIDEO | ATTACHMENT | QUIZ | LAB | EXTERNAL_LINK
            @NotBlank String title,
            String dueAt, // yyyy-MM-ddTHH:mm (optional)
            Double points) {
    }

    public record ModuleResponse(Long id, Long courseId, String weekLabel, String title, List<ModuleItemResponse> items) {
    }

    // --------------------------------------------------------------- invoices

    public record InvoiceRequest(
            @NotNull Long studentId,
            @NotBlank String description,
            @NotBlank String dueDate, // yyyy-MM-dd
            @PositiveOrZero double amount) {
    }

    public record InvoiceStatusRequest(@NotBlank String status) { // PAID | UNPAID | PARTIAL
    }

    public record InvoiceResponse(
            Long id,
            Long studentId,
            String studentName,
            String invoiceNumber,
            String description,
            String issueDate,
            String dueDate,
            double amount,
            String status) {
    }

    // --------------------------------------------------- enrollment requests

    public record EnrollmentRequestResponse(
            Long id,
            Long studentId,
            String studentName,
            String courseCode,
            String courseTitle,
            String requestedAt,
            String status) {
    }
}
