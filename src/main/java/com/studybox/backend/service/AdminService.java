package com.studybox.backend.service;

import com.studybox.backend.domain.CatalogCourse;
import com.studybox.backend.domain.Course;
import com.studybox.backend.domain.CourseInstructor;
import com.studybox.backend.domain.Enrollment;
import com.studybox.backend.domain.GradeCategory;
import com.studybox.backend.domain.GradeItem;
import com.studybox.backend.domain.Student;
import com.studybox.backend.dto.*;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.CatalogCourseRepository;
import com.studybox.backend.repository.CourseInstructorRepository;
import com.studybox.backend.repository.CourseRepository;
import com.studybox.backend.repository.EnrollmentRepository;
import com.studybox.backend.repository.GradeCategoryRepository;
import com.studybox.backend.repository.GradeItemRepository;
import com.studybox.backend.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The Super Admin's data-management layer: create students, assign them to courses, and
 * publish grades. Everything here writes to the same tables the mobile app and the student
 * dashboard read from, so a change made here is immediately visible to that student the next
 * time they refresh — there's no separate "admin copy" of the data.
 */
@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final GradeCategoryRepository gradeCategoryRepository;
    private final GradeItemRepository gradeItemRepository;
    private final GradeService gradeService;
    private final PasswordEncoder passwordEncoder;
    private final CatalogCourseRepository catalogCourseRepository;

    public AdminService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            CourseInstructorRepository courseInstructorRepository,
            GradeCategoryRepository gradeCategoryRepository,
            GradeItemRepository gradeItemRepository,
            GradeService gradeService,
            PasswordEncoder passwordEncoder,
            CatalogCourseRepository catalogCourseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.gradeCategoryRepository = gradeCategoryRepository;
        this.gradeItemRepository = gradeItemRepository;
        this.gradeService = gradeService;
        this.passwordEncoder = passwordEncoder;
        this.catalogCourseRepository = catalogCourseRepository;
    }

    // ---------------------------------------------------------------- overview

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        long postedGrades = gradeItemRepository.findAll().stream().filter(GradeItem::isPosted).count();
        return new AdminOverviewResponse(
                (int) studentRepository.count(),
                (int) courseRepository.count(),
                (int) enrollmentRepository.count(),
                (int) postedGrades);
    }

    // ---------------------------------------------------------------- students

    @Transactional(readOnly = true)
    public List<AdminStudentResponse> listStudents() {
        List<Student> students = studentRepository.findAll();
        Map<Long, List<Enrollment>> byStudent = enrollmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getStudent().getId()));
        return students.stream()
                .map(s -> toStudentResponse(s, byStudent.getOrDefault(s.getId(), List.of())))
                .sorted(Comparator.comparing(AdminStudentResponse::fullName))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminStudentResponse getStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        return toStudentResponse(student, enrollmentRepository.findByStudentId(id));
    }

    @Transactional
    public AdminStudentResponse createStudent(AdminStudentCreateRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("A student with this email already exists");
        }
        Student student = Student.builder()
                .studentNumber(generateStudentNumber())
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .programme(request.programme() != null && !request.programme().isBlank()
                        ? request.programme()
                        : "Bachelor of Science (BSc) (Honours) in Computer Science")
                .build();
        studentRepository.save(student);
        return toStudentResponse(student, List.of());
    }

    private String generateStudentNumber() {
        // A different range than the seed's demo IDs (100xxx) so the two never collide.
        return "ST-" + (200000 + new Random().nextInt(699999));
    }

    private AdminStudentResponse toStudentResponse(Student s, List<Enrollment> enrollments) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<AdminCourseRefResponse> courses = enrollments.stream()
                .map(e -> new AdminCourseRefResponse(
                        e.getId(), e.getCourse().getId(), e.getCourse().getCode(),
                        e.getCourse().getTitle(), e.getCourse().getColorHex()))
                .sorted(Comparator.comparing(AdminCourseRefResponse::code))
                .toList();
        return new AdminStudentResponse(
                s.getId(),
                s.getStudentNumber(),
                s.getFullName(),
                s.getEmail(),
                s.getProgramme(),
                s.getCreatedAt() != null ? s.getCreatedAt().format(fmt) : null,
                s.getLastLoginAt() != null ? s.getLastLoginAt().format(fmt) : null,
                s.getTotalLogins(),
                courses);
    }

    // ---------------------------------------------------------------- courses

    @Transactional(readOnly = true)
    public List<AdminCourseResponse> listCourses() {
        List<Course> courses = courseRepository.findAll();
        List<Long> ids = courses.stream().map(Course::getId).toList();

        Map<Long, List<CourseInstructor>> instructorsByCourse = courseInstructorRepository.findByCourseIdIn(ids).stream()
                .collect(Collectors.groupingBy(ci -> ci.getCourse().getId()));
        Map<Long, Long> enrollCountByCourse = enrollmentRepository.findByCourseIdIn(ids).stream()
                .collect(Collectors.groupingBy(e -> e.getCourse().getId(), Collectors.counting()));

        return courses.stream()
                .map(c -> new AdminCourseResponse(
                        c.getId(), c.getCode(), c.getTitle(), c.getCredits(), c.getTerm(), c.getColorHex(), c.getIcon(),
                        instructorsByCourse.getOrDefault(c.getId(), List.of()).stream()
                                .map(ci -> ci.getEducator().getFullName())
                                .toList(),
                        enrollCountByCourse.getOrDefault(c.getId(), 0L).intValue(),
                        c.getDescription()))
                .sorted(Comparator.comparing(AdminCourseResponse::code))
                .toList();
    }

    @Transactional
    public AdminCourseResponse createCourse(AdminCourseCreateRequest request) {
        if (courseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BadRequestException("A course with the code " + request.code() + " already exists");
        }
        Course course = Course.builder()
                .code(request.code())
                .title(request.title())
                .credits(request.credits())
                .term(request.term() != null && !request.term().isBlank() ? request.term() : "Current Semester")
                .colorHex(request.colorHex() != null && !request.colorHex().isBlank() ? request.colorHex() : "#6366F1")
                .icon(request.icon() != null && !request.icon().isBlank() ? request.icon() : "book")
                .description(request.description())
                .build();
        courseRepository.save(course);

        // Also list it in the registration catalogue so students can request to join it.
        if (catalogCourseRepository.findByCodeIgnoreCase(course.getCode()).isEmpty()) {
            catalogCourseRepository.save(CatalogCourse.builder()
                    .code(course.getCode())
                    .title(course.getTitle())
                    .credits(course.getCredits())
                    .term(course.getTerm())
                    .category("Core")
                    .description(course.getDescription())
                    .build());
        }
        return new AdminCourseResponse(
                course.getId(), course.getCode(), course.getTitle(), course.getCredits(), course.getTerm(),
                course.getColorHex(), course.getIcon(), List.of(), 0, course.getDescription());
    }

    // ------------------------------------------------------------ enrollments

    @Transactional(readOnly = true)
    public List<AdminEnrollmentResponse> listEnrollments(Long studentId, Long courseId) {
        List<Enrollment> enrollments;
        if (studentId != null) {
            enrollments = enrollmentRepository.findByStudentId(studentId);
        } else if (courseId != null) {
            enrollments = enrollmentRepository.findByCourseIdIn(List.of(courseId));
        } else {
            enrollments = enrollmentRepository.findAll();
        }
        return enrollments.stream()
                .map(this::toEnrollmentResponse)
                .sorted(Comparator.comparing(AdminEnrollmentResponse::studentName))
                .toList();
    }

    @Transactional
    public AdminEnrollmentResponse enroll(AdminEnrollmentCreateRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found"));

        if (enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).isPresent()) {
            throw new BadRequestException(student.getFullName() + " is already enrolled in " + course.getCode());
        }

        Enrollment enrollment = Enrollment.builder().student(student).course(course).build();
        enrollmentRepository.save(enrollment);
        return toEnrollmentResponse(enrollment);
    }

    @Transactional
    public void unenroll(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
    }

    private AdminEnrollmentResponse toEnrollmentResponse(Enrollment e) {
        return new AdminEnrollmentResponse(
                e.getId(), e.getStudent().getId(), e.getStudent().getFullName(),
                e.getCourse().getId(), e.getCourse().getCode(), e.getCourse().getTitle(),
                e.getPresences(), e.getLates(), e.getAuthorizedAbsences(), e.getUnauthorizedAbsences());
    }

    // ----------------------------------------------------------------- grades

    @Transactional
    public AdminGradeResponse publishGrade(AdminGradePublishRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found"));

        if (enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).isEmpty()) {
            throw new BadRequestException(
                    student.getFullName() + " is not enrolled in " + course.getCode() + " yet — enroll them first");
        }

        List<GradeCategory> categories = gradeCategoryRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
        GradeCategory category = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(request.categoryName()))
                .findFirst()
                .orElseGet(() -> gradeCategoryRepository.save(GradeCategory.builder()
                        .course(course)
                        .name(request.categoryName())
                        .weightPercent(request.categoryWeightPercent() > 0 ? request.categoryWeightPercent() : 100)
                        .orderIndex(categories.size())
                        .build()));

        GradeItem item = GradeItem.builder()
                .category(category)
                .student(student)
                .name(request.itemName())
                .maxScore(request.maxScore())
                .scoreEarned(request.scoreEarned())
                .posted(true)
                .orderIndex(category.getItems().size())
                .build();
        gradeItemRepository.save(item);

        GradeCourseResponse recomputed = gradeService.courseForStudent(student.getId(), course.getId());

        return new AdminGradeResponse(
                item.getId(), student.getId(), student.getFullName(), course.getId(), course.getCode(),
                category.getName(), item.getName(), item.getMaxScore(), item.getScoreEarned(), item.isPosted(),
                recomputed != null ? recomputed.totalPercent() : null);
    }

    @Transactional(readOnly = true)
    public List<AdminGradeResponse> listGrades(Long studentId, Long courseId) {
        if (studentId == null) {
            throw new BadRequestException("studentId is required");
        }
        List<GradeItem> items = courseId != null
                ? gradeItemRepository.findByCategoryCourseIdAndStudentId(courseId, studentId)
                : gradeItemRepository.findByStudentId(studentId);

        Double totalPercent = null;
        if (courseId != null) {
            GradeCourseResponse recomputed = gradeService.courseForStudent(studentId, courseId);
            totalPercent = recomputed != null ? recomputed.totalPercent() : null;
        }
        Double finalTotal = totalPercent;

        return items.stream()
                .map(item -> new AdminGradeResponse(
                        item.getId(), item.getStudent().getId(), item.getStudent().getFullName(),
                        item.getCategory().getCourse().getId(), item.getCategory().getCourse().getCode(),
                        item.getCategory().getName(), item.getName(), item.getMaxScore(), item.getScoreEarned(),
                        item.isPosted(), finalTotal))
                .toList();
    }
}
