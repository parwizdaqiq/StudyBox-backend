package com.studybox.backend.controller;

import com.studybox.backend.dto.*;
import com.studybox.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The Super Admin API — everything here requires an ADMIN-role JWT (see
 * {@code SecurityConfig}, which gates all of {@code /api/admin/**} except {@code /auth} behind
 * {@code hasRole("ADMIN")}). This is where student accounts, course enrollments and grades are
 * actually created; the student-facing endpoints under {@code /api} only ever read what's
 * written here.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse overview() {
        return adminService.overview();
    }

    // ---------------------------------------------------------------- students

    @GetMapping("/students")
    public List<AdminStudentResponse> listStudents() {
        return adminService.listStudents();
    }

    @GetMapping("/students/{id}")
    public AdminStudentResponse getStudent(@PathVariable Long id) {
        return adminService.getStudent(id);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminStudentResponse createStudent(@Valid @RequestBody AdminStudentCreateRequest request) {
        return adminService.createStudent(request);
    }

    // ---------------------------------------------------------------- courses

    @GetMapping("/courses")
    public List<AdminCourseResponse> listCourses() {
        return adminService.listCourses();
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCourseResponse createCourse(@Valid @RequestBody AdminCourseCreateRequest request) {
        return adminService.createCourse(request);
    }

    // ------------------------------------------------------------ enrollments

    @GetMapping("/enrollments")
    public List<AdminEnrollmentResponse> listEnrollments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId) {
        return adminService.listEnrollments(studentId, courseId);
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminEnrollmentResponse enroll(@Valid @RequestBody AdminEnrollmentCreateRequest request) {
        return adminService.enroll(request);
    }

    @DeleteMapping("/enrollments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable Long id) {
        adminService.unenroll(id);
    }

    // ----------------------------------------------------------------- grades

    @GetMapping("/grades")
    public List<AdminGradeResponse> listGrades(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long courseId) {
        return adminService.listGrades(studentId, courseId);
    }

    @PostMapping("/grades")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminGradeResponse publishGrade(@Valid @RequestBody AdminGradePublishRequest request) {
        return adminService.publishGrade(request);
    }
}
