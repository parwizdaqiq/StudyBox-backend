package com.studybox.backend.controller;

import com.studybox.backend.dto.AdminCourseCreateRequest;
import com.studybox.backend.dto.AdminCourseResponse;
import com.studybox.backend.dto.AdminDtos;
import com.studybox.backend.dto.AdminGradeResponse;
import com.studybox.backend.dto.AdminStudentResponse;
import com.studybox.backend.dto.AssignmentResponse;
import com.studybox.backend.dto.TimetableEntryResponse;
import com.studybox.backend.repository.AdminAccountRepository;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The rest of the Super Admin API (ADMIN-role JWT required — see {@code SecurityConfig}):
 * edit/delete students and courses, instructors, attendance, grade edits, assignments,
 * announcements, timetable, course modules, invoices, and registration-request decisions.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    private final AdminContentService service;
    private final AdminAccountRepository adminAccountRepository;

    public AdminContentController(AdminContentService service, AdminAccountRepository adminAccountRepository) {
        this.service = service;
        this.adminAccountRepository = adminAccountRepository;
    }

    // ---------------------------------------------------------------- students

    @PutMapping("/students/{id}")
    public AdminStudentResponse updateStudent(@PathVariable Long id, @Valid @RequestBody AdminDtos.StudentUpdateRequest request) {
        return service.updateStudent(id, request);
    }

    @PostMapping("/students/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody AdminDtos.PasswordResetRequest request) {
        service.resetPassword(id, request);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        service.deleteStudent(id);
    }

    // ---------------------------------------------------------------- courses

    @PutMapping("/courses/{id}")
    public AdminCourseResponse updateCourse(@PathVariable Long id, @Valid @RequestBody AdminCourseCreateRequest request) {
        return service.updateCourse(id, request);
    }

    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
    }

    @PutMapping("/courses/{id}/instructors")
    public AdminCourseResponse setInstructors(@PathVariable Long id, @Valid @RequestBody AdminDtos.CourseInstructorsRequest request) {
        return service.setInstructors(id, request);
    }

    // -------------------------------------------------------------- educators

    @GetMapping("/educators")
    public List<AdminDtos.EducatorResponse> educators() {
        return service.listEducators();
    }

    @PostMapping("/educators")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.EducatorResponse createEducator(@Valid @RequestBody AdminDtos.EducatorRequest request) {
        return service.createEducator(request);
    }

    @PutMapping("/educators/{id}")
    public AdminDtos.EducatorResponse updateEducator(@PathVariable Long id, @Valid @RequestBody AdminDtos.EducatorRequest request) {
        return service.updateEducator(id, request);
    }

    @DeleteMapping("/educators/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEducator(@PathVariable Long id) {
        service.deleteEducator(id);
    }

    // ------------------------------------------------------------- attendance

    @GetMapping("/sessions")
    public List<AdminDtos.SessionResponse> sessions(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId) {
        return service.listSessions(studentId, courseId);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.SessionResponse recordSession(@Valid @RequestBody AdminDtos.SessionCreateRequest request) {
        return service.recordSession(request);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long id) {
        service.deleteSession(id);
    }

    // ----------------------------------------------------------------- grades

    @PutMapping("/grades/{id}")
    public AdminGradeResponse updateGrade(@PathVariable Long id, @Valid @RequestBody AdminDtos.GradeUpdateRequest request) {
        return service.updateGrade(id, request);
    }

    @DeleteMapping("/grades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGrade(@PathVariable Long id) {
        service.deleteGrade(id);
    }

    @GetMapping("/grade-categories")
    public List<AdminDtos.GradeCategoryResponse> gradeCategories(@RequestParam Long courseId) {
        return service.listGradeCategories(courseId);
    }

    @PutMapping("/grade-categories/{id}")
    public AdminDtos.GradeCategoryResponse updateGradeCategory(@PathVariable Long id, @Valid @RequestBody AdminDtos.GradeCategoryRequest request) {
        return service.updateGradeCategory(id, request);
    }

    @DeleteMapping("/grade-categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGradeCategory(@PathVariable Long id) {
        service.deleteGradeCategory(id);
    }

    // ------------------------------------------------------------ assignments

    @GetMapping("/assignments")
    public List<AssignmentResponse> assignments(@RequestParam(required = false) Long courseId) {
        return service.listAssignments(courseId);
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse createAssignment(@Valid @RequestBody AdminDtos.AssignmentRequest request) {
        return service.createAssignment(request);
    }

    @PutMapping("/assignments/{id}")
    public AssignmentResponse updateAssignment(@PathVariable Long id, @Valid @RequestBody AdminDtos.AssignmentRequest request) {
        return service.updateAssignment(id, request);
    }

    @DeleteMapping("/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssignment(@PathVariable Long id) {
        service.deleteAssignment(id);
    }

    // ---------------------------------------------------------- announcements

    @GetMapping("/announcements")
    public List<AdminDtos.AnnouncementResponse> announcements() {
        return service.listAnnouncements();
    }

    @PostMapping("/announcements")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.AnnouncementResponse createAnnouncement(
            @AuthenticationPrincipal CurrentUser user,
            @Valid @RequestBody AdminDtos.AnnouncementRequest request) {
        String postedBy = user == null ? null : adminAccountRepository.findById(user.studentId())
                .map(a -> a.getFullName())
                .orElse(null);
        return service.createAnnouncement(request, postedBy);
    }

    @DeleteMapping("/announcements/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnnouncement(@PathVariable Long id) {
        service.deleteAnnouncement(id);
    }

    // -------------------------------------------------------------- timetable

    @GetMapping("/timetable")
    public List<TimetableEntryResponse> timetable(@RequestParam(required = false) Long courseId) {
        return service.listTimetable(courseId);
    }

    @PostMapping("/timetable")
    @ResponseStatus(HttpStatus.CREATED)
    public TimetableEntryResponse createTimetableEntry(@Valid @RequestBody AdminDtos.TimetableRequest request) {
        return service.createTimetableEntry(request);
    }

    @DeleteMapping("/timetable/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimetableEntry(@PathVariable Long id) {
        service.deleteTimetableEntry(id);
    }

    // ---------------------------------------------------------------- modules

    @GetMapping("/modules")
    public List<AdminDtos.ModuleResponse> modules(@RequestParam Long courseId) {
        return service.listModules(courseId);
    }

    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.ModuleResponse createModule(@Valid @RequestBody AdminDtos.ModuleRequest request) {
        return service.createModule(request);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable Long id) {
        service.deleteModule(id);
    }

    @PostMapping("/modules/{id}/items")
    public AdminDtos.ModuleResponse addModuleItem(@PathVariable Long id, @Valid @RequestBody AdminDtos.ModuleItemRequest request) {
        return service.addModuleItem(id, request);
    }

    @DeleteMapping("/modules/{moduleId}/items/{itemId}")
    public AdminDtos.ModuleResponse deleteModuleItem(@PathVariable Long moduleId, @PathVariable Long itemId) {
        return service.deleteModuleItem(moduleId, itemId);
    }

    // --------------------------------------------------------------- invoices

    @GetMapping("/invoices")
    public List<AdminDtos.InvoiceResponse> invoices(@RequestParam(required = false) Long studentId) {
        return service.listInvoices(studentId);
    }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.InvoiceResponse createInvoice(@Valid @RequestBody AdminDtos.InvoiceRequest request) {
        return service.createInvoice(request);
    }

    @PutMapping("/invoices/{id}/status")
    public AdminDtos.InvoiceResponse setInvoiceStatus(@PathVariable Long id, @Valid @RequestBody AdminDtos.InvoiceStatusRequest request) {
        return service.setInvoiceStatus(id, request);
    }

    @DeleteMapping("/invoices/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable Long id) {
        service.deleteInvoice(id);
    }

    // --------------------------------------------------- enrollment requests

    @GetMapping("/enrollment-requests")
    public List<AdminDtos.EnrollmentRequestResponse> enrollmentRequests() {
        return service.listEnrollmentRequests();
    }

    @PostMapping("/enrollment-requests/{id}/approve")
    public AdminDtos.EnrollmentRequestResponse approve(@PathVariable Long id) {
        return service.decideEnrollmentRequest(id, true);
    }

    @PostMapping("/enrollment-requests/{id}/reject")
    public AdminDtos.EnrollmentRequestResponse reject(@PathVariable Long id) {
        return service.decideEnrollmentRequest(id, false);
    }
}
