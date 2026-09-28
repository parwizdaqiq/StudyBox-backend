package com.studybox.backend.service;

import com.studybox.backend.domain.*;
import com.studybox.backend.dto.*;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseModuleRepository courseModuleRepository;

    public CourseService(
            EnrollmentRepository enrollmentRepository,
            CourseInstructorRepository courseInstructorRepository,
            CourseModuleRepository courseModuleRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.courseModuleRepository = courseModuleRepository;
    }

    /** Course ids the given student is currently enrolled in. */
    @Transactional(readOnly = true)
    public List<Long> enrolledCourseIds(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(e -> e.getCourse().getId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Enrollment> enrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> listMyCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).toList();

        Map<Long, List<CourseInstructor>> instructorsByCourse = courseInstructorRepository
                .findByCourseIdIn(courseIds).stream()
                .collect(Collectors.groupingBy(ci -> ci.getCourse().getId()));

        return enrollments.stream()
                .map(e -> toSummary(e.getCourse(), instructorsByCourse.getOrDefault(e.getCourse().getId(), List.of())))
                .sorted(Comparator.comparing(CourseSummaryResponse::code))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new NotFoundException("You are not enrolled in this course"));
        Course course = enrollment.getCourse();

        List<CourseInstructor> instructors = courseInstructorRepository.findByCourseId(courseId);
        List<CourseModule> modules = courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        CourseSummaryResponse summary = toSummary(course, instructors);

        AttendanceResponse attendance = new AttendanceResponse(
                enrollment.getUnauthorizedAbsences(),
                enrollment.getAuthorizedAbsences(),
                enrollment.getLates(),
                enrollment.getPresences());

        List<InstructorResponse> instructorResponses = instructors.stream()
                .map(ci -> new InstructorResponse(
                        ci.getEducator().getId(),
                        ci.getEducator().getFullName(),
                        ci.getRoleInCourse() != null ? ci.getRoleInCourse() : ci.getEducator().getTitle(),
                        ci.getEducator().getEmail(),
                        ci.getEducator().getAvatarInitials(),
                        List.of(course.getTitle())))
                .toList();

        List<ModuleResponse> moduleResponses = modules.stream()
                .map(this::toModuleResponse)
                .toList();

        return new CourseDetailResponse(summary, attendance, instructorResponses, moduleResponses);
    }

    private ModuleResponse toModuleResponse(CourseModule module) {
        List<ModuleItemResponse> items = module.getItems().stream()
                .map(item -> new ModuleItemResponse(
                        item.getId(),
                        item.getType().name(),
                        item.getTitle(),
                        item.getDueAt() != null ? item.getDueAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                        item.getPoints()))
                .toList();
        return new ModuleResponse(module.getId(), module.getWeekLabel(), module.getTitle(), items);
    }

    private CourseSummaryResponse toSummary(Course course, List<CourseInstructor> instructors) {
        List<String> instructorNames = instructors.stream()
                .map(ci -> ci.getEducator().getFullName())
                .toList();
        return new CourseSummaryResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getCredits(),
                course.getTerm(),
                course.getColorHex(),
                course.getIcon(),
                instructorNames);
    }
}
