package com.studybox.backend.service;

import com.studybox.backend.domain.*;
import com.studybox.backend.dto.AdminCourseCreateRequest;
import com.studybox.backend.dto.AdminCourseResponse;
import com.studybox.backend.dto.AdminDtos;
import com.studybox.backend.dto.AdminGradeResponse;
import com.studybox.backend.dto.AdminStudentResponse;
import com.studybox.backend.dto.AssignmentResponse;
import com.studybox.backend.dto.GradeCourseResponse;
import com.studybox.backend.dto.ModuleItemResponse;
import com.studybox.backend.dto.TimetableEntryResponse;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Everything the Super Admin can manage beyond the basic create-student / create-course /
 * enroll / publish-grade flow in {@link AdminService}: editing and deleting students and
 * courses, instructors, attendance, grade edits, assignments, announcements, the timetable,
 * weekly course modules, invoices, and approving students' registration requests.
 *
 * <p>Every write lands in the same tables the student endpoints under {@code /api/**} read,
 * so the mobile app and the student web dashboard pick it up on their next refresh.
 */
@Service
public class AdminContentService {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EducatorRepository educatorRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeCategoryRepository gradeCategoryRepository;
    private final GradeItemRepository gradeItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final InvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ConversationRepository conversationRepository;
    private final FileResourceRepository fileResourceRepository;
    private final DiscussionThreadRepository discussionThreadRepository;
    private final CatalogCourseRepository catalogCourseRepository;
    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final AdminService adminService;
    private final GradeService gradeService;
    private final PasswordEncoder passwordEncoder;

    public AdminContentService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EducatorRepository educatorRepository,
            CourseInstructorRepository courseInstructorRepository,
            EnrollmentRepository enrollmentRepository,
            GradeCategoryRepository gradeCategoryRepository,
            GradeItemRepository gradeItemRepository,
            AssignmentRepository assignmentRepository,
            AnnouncementRepository announcementRepository,
            TimetableEntryRepository timetableEntryRepository,
            CourseModuleRepository courseModuleRepository,
            InvoiceRepository invoiceRepository,
            InstallmentRepository installmentRepository,
            ClassSessionRepository classSessionRepository,
            ConversationRepository conversationRepository,
            FileResourceRepository fileResourceRepository,
            DiscussionThreadRepository discussionThreadRepository,
            CatalogCourseRepository catalogCourseRepository,
            EnrollmentRequestRepository enrollmentRequestRepository,
            AdminService adminService,
            GradeService gradeService,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.educatorRepository = educatorRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeCategoryRepository = gradeCategoryRepository;
        this.gradeItemRepository = gradeItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.classSessionRepository = classSessionRepository;
        this.conversationRepository = conversationRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.discussionThreadRepository = discussionThreadRepository;
        this.catalogCourseRepository = catalogCourseRepository;
        this.enrollmentRequestRepository = enrollmentRequestRepository;
        this.adminService = adminService;
        this.gradeService = gradeService;
        this.passwordEncoder = passwordEncoder;
    }

    // =================================================================== students

    @Transactional
    public AdminStudentResponse updateStudent(Long id, AdminDtos.StudentUpdateRequest request) {
        Student student = student(id);
        if (!student.getEmail().equalsIgnoreCase(request.email())
                && studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Another student already uses " + request.email());
        }
        student.setFullName(request.fullName().trim());
        student.setEmail(request.email().trim());
        if (request.programme() != null && !request.programme().isBlank()) {
            student.setProgramme(request.programme().trim());
        }
        studentRepository.save(student);
        return adminService.getStudent(id);
    }

    @Transactional
    public void resetPassword(Long id, AdminDtos.PasswordResetRequest request) {
        Student student = student(id);
        student.setPasswordHash(passwordEncoder.encode(request.password()));
        studentRepository.save(student);
    }

    /** Removes the student and everything that belongs only to them. */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = student(id);
        gradeItemRepository.deleteAll(gradeItemRepository.findByStudentId(id));
        classSessionRepository.deleteAll(classSessionRepository.findByStudentIdOrderBySessionDateDesc(id));
        invoiceRepository.deleteAll(invoiceRepository.findByStudentIdOrderByIssueDateDesc(id));
        installmentRepository.deleteAll(installmentRepository.findByStudentIdOrderByInstallmentNumberAsc(id));
        conversationRepository.deleteAll(conversationRepository.findByStudentId(id));
        fileResourceRepository.deleteAll(fileResourceRepository.findByStudentIdOrderByUploadedAtDesc(id));
        enrollmentRequestRepository.deleteAll(enrollmentRequestRepository.findByStudentIdOrderByRequestedAtDesc(id));
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudentId(id));
        studentRepository.flush();
        studentRepository.delete(student);
    }

    // ==================================================================== courses

    @Transactional
    public AdminCourseResponse updateCourse(Long id, AdminCourseCreateRequest request) {
        Course course = course(id);
        String oldCode = course.getCode();
        if (!oldCode.equalsIgnoreCase(request.code()) && courseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BadRequestException("A course with the code " + request.code() + " already exists");
        }
        course.setCode(request.code().trim());
        course.setTitle(request.title().trim());
        course.setCredits(request.credits());
        if (notBlank(request.term())) course.setTerm(request.term().trim());
        if (notBlank(request.colorHex())) course.setColorHex(request.colorHex().trim());
        if (notBlank(request.icon())) course.setIcon(request.icon().trim());
        course.setDescription(request.description());
        courseRepository.save(course);

        // Keep the registration catalogue entry in step with the course.
        CatalogCourse catalog = catalogCourseRepository.findByCodeIgnoreCase(oldCode)
                .orElseGet(CatalogCourse::new);
        catalog.setCode(course.getCode());
        catalog.setTitle(course.getTitle());
        catalog.setCredits(course.getCredits());
        catalog.setTerm(course.getTerm());
        catalog.setCategory(catalog.getCategory() != null ? catalog.getCategory() : "Core");
        catalog.setDescription(course.getDescription());
        catalogCourseRepository.save(catalog);

        return findCourseResponse(id);
    }

    /** Deletes a course and all of its content (modules, assignments, grades, timetable…). */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = course(id);

        // Grade categories cascade to their items; assignments must go after the items that
        // reference them.
        gradeCategoryRepository.deleteAll(gradeCategoryRepository.findByCourseIdOrderByOrderIndexAsc(id));
        gradeCategoryRepository.flush();
        assignmentRepository.deleteAll(assignmentRepository.findByCourseId(id));
        courseModuleRepository.deleteAll(courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(id));
        timetableEntryRepository.deleteAll(timetableEntryRepository.findByCourseIdIn(List.of(id)));
        discussionThreadRepository.deleteAll(discussionThreadRepository.findByCourseId(id));
        announcementRepository.deleteAll(announcementRepository.findByCourseIdInOrderByPostedAtDesc(List.of(id)));
        classSessionRepository.deleteAll(classSessionRepository.findAll().stream()
                .filter(s -> s.getCourse().getId().equals(id)).toList());
        fileResourceRepository.findAll().stream()
                .filter(f -> f.getCourse() != null && f.getCourse().getId().equals(id))
                .forEach(f -> f.setCourse(null));
        conversationRepository.findAll().stream()
                .filter(c -> c.getCourse() != null && c.getCourse().getId().equals(id))
                .forEach(c -> c.setCourse(null));
        courseInstructorRepository.deleteAll(courseInstructorRepository.findByCourseId(id));
        enrollmentRepository.deleteAll(enrollmentRepository.findByCourseIdIn(List.of(id)));

        catalogCourseRepository.findByCodeIgnoreCase(course.getCode()).ifPresent(catalog -> {
            enrollmentRequestRepository.deleteAll(enrollmentRequestRepository.findAll().stream()
                    .filter(r -> r.getCatalogCourse().getId().equals(catalog.getId())).toList());
            catalogCourseRepository.delete(catalog);
        });

        courseRepository.flush();
        courseRepository.delete(course);
    }

    /** Replaces the list of instructors teaching a course. */
    @Transactional
    public AdminCourseResponse setInstructors(Long courseId, AdminDtos.CourseInstructorsRequest request) {
        Course course = course(courseId);
        courseInstructorRepository.deleteAll(courseInstructorRepository.findByCourseId(courseId));
        courseInstructorRepository.flush();
        for (Long educatorId : request.educatorIds().stream().distinct().toList()) {
            Educator educator = educatorRepository.findById(educatorId)
                    .orElseThrow(() -> new NotFoundException("Instructor " + educatorId + " not found"));
            courseInstructorRepository.save(CourseInstructor.builder()
                    .course(course)
                    .educator(educator)
                    .roleInCourse(educator.getTitle())
                    .build());
        }
        return findCourseResponse(courseId);
    }

    private AdminCourseResponse findCourseResponse(Long id) {
        return adminService.listCourses().stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    // ================================================================== educators

    @Transactional(readOnly = true)
    public List<AdminDtos.EducatorResponse> listEducators() {
        Map<Long, List<CourseInstructor>> byEducator = courseInstructorRepository.findAll().stream()
                .collect(Collectors.groupingBy(ci -> ci.getEducator().getId()));
        return educatorRepository.findAll().stream()
                .map(e -> toEducatorResponse(e, byEducator.getOrDefault(e.getId(), List.of())))
                .sorted(Comparator.comparing(AdminDtos.EducatorResponse::fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public AdminDtos.EducatorResponse createEducator(AdminDtos.EducatorRequest request) {
        Educator educator = Educator.builder()
                .fullName(request.fullName().trim())
                .title(notBlank(request.title()) ? request.title().trim() : "Lecturer")
                .email(request.email().trim())
                .avatarInitials(initialsOf(request.fullName()))
                .build();
        educatorRepository.save(educator);
        return toEducatorResponse(educator, List.of());
    }

    @Transactional
    public AdminDtos.EducatorResponse updateEducator(Long id, AdminDtos.EducatorRequest request) {
        Educator educator = educatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instructor not found"));
        educator.setFullName(request.fullName().trim());
        educator.setTitle(notBlank(request.title()) ? request.title().trim() : educator.getTitle());
        educator.setEmail(request.email().trim());
        educator.setAvatarInitials(initialsOf(request.fullName()));
        educatorRepository.save(educator);
        List<CourseInstructor> links = courseInstructorRepository.findAll().stream()
                .filter(ci -> ci.getEducator().getId().equals(id)).toList();
        return toEducatorResponse(educator, links);
    }

    @Transactional
    public void deleteEducator(Long id) {
        Educator educator = educatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instructor not found"));
        courseInstructorRepository.deleteAll(courseInstructorRepository.findAll().stream()
                .filter(ci -> ci.getEducator().getId().equals(id)).toList());
        courseInstructorRepository.flush();
        educatorRepository.delete(educator);
    }

    private AdminDtos.EducatorResponse toEducatorResponse(Educator e, List<CourseInstructor> links) {
        return new AdminDtos.EducatorResponse(
                e.getId(), e.getFullName(), e.getTitle(), e.getEmail(), e.getAvatarInitials(),
                links.stream().map(ci -> ci.getCourse().getId()).toList(),
                links.stream().map(ci -> ci.getCourse().getCode()).sorted().toList());
    }

    // ================================================================= attendance

    @Transactional(readOnly = true)
    public List<AdminDtos.SessionResponse> listSessions(Long studentId, Long courseId) {
        List<ClassSession> sessions = studentId != null
                ? (courseId != null
                    ? classSessionRepository.findByStudentIdAndCourseIdOrderBySessionDateDesc(studentId, courseId)
                    : classSessionRepository.findByStudentIdOrderBySessionDateDesc(studentId))
                : classSessionRepository.findAll().stream()
                    .filter(s -> courseId == null || s.getCourse().getId().equals(courseId))
                    .sorted(Comparator.comparing(ClassSession::getSessionDate).reversed())
                    .toList();
        return sessions.stream().map(this::toSessionResponse).toList();
    }

    /**
     * Records one attendance mark for a student in a course. This both adds a row to their
     * session log and bumps the matching tally on their enrollment, so the attendance
     * percentages on the student's dashboard stay consistent with the log.
     */
    @Transactional
    public AdminDtos.SessionResponse recordSession(AdminDtos.SessionCreateRequest request) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(request.studentId(), request.courseId())
                .orElseThrow(() -> new BadRequestException("That student isn't enrolled in this course"));
        SessionStatus status = parseEnum(SessionStatus.class, request.status(), "attendance status");

        ClassSession session = ClassSession.builder()
                .student(enrollment.getStudent())
                .course(enrollment.getCourse())
                .sessionDate(parseDate(request.sessionDate(), "session date"))
                .sessionLabel(notBlank(request.sessionLabel()) ? request.sessionLabel().trim() : "Live session")
                .status(status)
                .build();
        classSessionRepository.save(session);
        adjustTally(enrollment, status, +1);
        enrollmentRepository.save(enrollment);
        return toSessionResponse(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        ClassSession session = classSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance record not found"));
        enrollmentRepository.findByStudentIdAndCourseId(session.getStudent().getId(), session.getCourse().getId())
                .ifPresent(enrollment -> {
                    adjustTally(enrollment, session.getStatus(), -1);
                    enrollmentRepository.save(enrollment);
                });
        classSessionRepository.delete(session);
    }

    private void adjustTally(Enrollment e, SessionStatus status, int delta) {
        switch (status) {
            case PRESENT -> e.setPresences(Math.max(0, e.getPresences() + delta));
            case LATE -> e.setLates(Math.max(0, e.getLates() + delta));
            case AUTHORIZED_ABSENCE -> e.setAuthorizedAbsences(Math.max(0, e.getAuthorizedAbsences() + delta));
            case UNAUTHORIZED_ABSENCE -> e.setUnauthorizedAbsences(Math.max(0, e.getUnauthorizedAbsences() + delta));
        }
    }

    private AdminDtos.SessionResponse toSessionResponse(ClassSession s) {
        return new AdminDtos.SessionResponse(
                s.getId(), s.getStudent().getId(), s.getStudent().getFullName(),
                s.getCourse().getId(), s.getCourse().getCode(),
                s.getSessionDate() != null ? s.getSessionDate().toString() : null,
                s.getSessionLabel(), s.getStatus().name());
    }

    // ===================================================================== grades

    @Transactional
    public AdminGradeResponse updateGrade(Long id, AdminDtos.GradeUpdateRequest request) {
        GradeItem item = gradeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grade not found"));
        if (request.scoreEarned() != null && request.scoreEarned() > request.maxScore()) {
            throw new BadRequestException("Score can't be higher than the maximum score");
        }
        item.setName(request.itemName().trim());
        item.setMaxScore(request.maxScore());
        item.setScoreEarned(request.scoreEarned());
        item.setPosted(request.posted());
        gradeItemRepository.save(item);
        return toGradeResponse(item);
    }

    @Transactional
    public void deleteGrade(Long id) {
        GradeItem item = gradeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grade not found"));
        GradeCategory category = item.getCategory();
        category.getItems().remove(item); // orphanRemoval deletes the row
        gradeCategoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.GradeCategoryResponse> listGradeCategories(Long courseId) {
        return gradeCategoryRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(c -> new AdminDtos.GradeCategoryResponse(c.getId(), courseId, c.getName(), c.getWeightPercent()))
                .toList();
    }

    @Transactional
    public AdminDtos.GradeCategoryResponse updateGradeCategory(Long id, AdminDtos.GradeCategoryRequest request) {
        GradeCategory category = gradeCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grade category not found"));
        category.setName(request.name().trim());
        category.setWeightPercent(request.weightPercent());
        gradeCategoryRepository.save(category);
        return new AdminDtos.GradeCategoryResponse(
                category.getId(), category.getCourse().getId(), category.getName(), category.getWeightPercent());
    }

    @Transactional
    public void deleteGradeCategory(Long id) {
        GradeCategory category = gradeCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grade category not found"));
        gradeCategoryRepository.delete(category);
    }

    private AdminGradeResponse toGradeResponse(GradeItem item) {
        Course course = item.getCategory().getCourse();
        Double total = null;
        if (item.getStudent() != null) {
            GradeCourseResponse recomputed = gradeService.courseForStudent(item.getStudent().getId(), course.getId());
            total = recomputed != null ? recomputed.totalPercent() : null;
        }
        return new AdminGradeResponse(
                item.getId(),
                item.getStudent() != null ? item.getStudent().getId() : null,
                item.getStudent() != null ? item.getStudent().getFullName() : "Whole class",
                course.getId(), course.getCode(), item.getCategory().getName(), item.getName(),
                item.getMaxScore(), item.getScoreEarned(), item.isPosted(), total);
    }

    // ================================================================ assignments

    @Transactional(readOnly = true)
    public List<AssignmentResponse> listAssignments(Long courseId) {
        List<Assignment> rows = courseId != null
                ? assignmentRepository.findByCourseId(courseId)
                : assignmentRepository.findAll();
        return rows.stream()
                .map(this::toAssignmentResponse)
                .sorted(Comparator.comparing(AssignmentResponse::dueAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public AssignmentResponse createAssignment(AdminDtos.AssignmentRequest request) {
        Assignment a = Assignment.builder()
                .course(course(request.courseId()))
                .title(request.title().trim())
                .type(parseEnum(AssignmentType.class, request.type(), "assignment type"))
                .categoryLabel(notBlank(request.categoryLabel()) ? request.categoryLabel().trim() : null)
                .availableFrom(parseDateTime(request.availableFrom(), "available-from date"))
                .dueAt(parseDateTime(request.dueAt(), "due date"))
                .pointsPossible(request.pointsPossible())
                .build();
        assignmentRepository.save(a);
        return toAssignmentResponse(a);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long id, AdminDtos.AssignmentRequest request) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        a.setCourse(course(request.courseId()));
        a.setTitle(request.title().trim());
        a.setType(parseEnum(AssignmentType.class, request.type(), "assignment type"));
        a.setCategoryLabel(notBlank(request.categoryLabel()) ? request.categoryLabel().trim() : null);
        a.setAvailableFrom(parseDateTime(request.availableFrom(), "available-from date"));
        a.setDueAt(parseDateTime(request.dueAt(), "due date"));
        a.setPointsPossible(request.pointsPossible());
        assignmentRepository.save(a);
        return toAssignmentResponse(a);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        // Detach any grade items that pointed at it rather than deleting published grades.
        gradeItemRepository.findAll().stream()
                .filter(g -> g.getAssignment() != null && g.getAssignment().getId().equals(id))
                .forEach(g -> g.setAssignment(null));
        gradeItemRepository.flush();
        assignmentRepository.delete(a);
    }

    private AssignmentResponse toAssignmentResponse(Assignment a) {
        Course c = a.getCourse();
        return new AssignmentResponse(
                a.getId(), c.getId(), c.getCode(), c.getTitle(), c.getColorHex(), a.getTitle(),
                a.getType().name(), a.getCategoryLabel(),
                a.getAvailableFrom() != null ? a.getAvailableFrom().format(ISO_DT) : null,
                a.getDueAt() != null ? a.getDueAt().format(ISO_DT) : null,
                a.getPointsPossible(), a.getScoreEarned(), a.getStatus().name());
    }

    // ============================================================== announcements

    @Transactional(readOnly = true)
    public List<AdminDtos.AnnouncementResponse> listAnnouncements() {
        return announcementRepository.findAllByOrderByPostedAtDesc().stream()
                .map(this::toAnnouncementResponse)
                .toList();
    }

    @Transactional
    public AdminDtos.AnnouncementResponse createAnnouncement(AdminDtos.AnnouncementRequest request, String postedBy) {
        Announcement a = Announcement.builder()
                .course(request.courseId() != null ? course(request.courseId()) : null)
                .title(request.title().trim())
                .body(request.body().trim())
                .postedAt(LocalDateTime.now())
                .postedBy(notBlank(postedBy) ? postedBy : "Programme Administrator")
                .build();
        announcementRepository.save(a);
        return toAnnouncementResponse(a);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.delete(announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found")));
    }

    private AdminDtos.AnnouncementResponse toAnnouncementResponse(Announcement a) {
        return new AdminDtos.AnnouncementResponse(
                a.getId(),
                a.getCourse() != null ? a.getCourse().getId() : null,
                a.getCourse() != null ? a.getCourse().getTitle() : "All students",
                a.getTitle(), a.getBody(),
                a.getPostedAt() != null ? a.getPostedAt().format(ISO_DT) : null,
                a.getPostedBy());
    }

    // ================================================================== timetable

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> listTimetable(Long courseId) {
        List<TimetableEntry> rows = courseId != null
                ? timetableEntryRepository.findByCourseIdIn(List.of(courseId))
                : timetableEntryRepository.findAll();
        return rows.stream()
                .sorted(Comparator.comparing(TimetableEntry::getDayOfWeek).thenComparing(TimetableEntry::getStartTime))
                .map(this::toTimetableResponse)
                .toList();
    }

    @Transactional
    public TimetableEntryResponse createTimetableEntry(AdminDtos.TimetableRequest request) {
        Course course = course(request.courseId());
        LocalTime start = parseTime(request.startTime(), "start time");
        LocalTime end = parseTime(request.endTime(), "end time");
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }
        TimetableEntry entry = TimetableEntry.builder()
                .course(course)
                .dayOfWeek(parseEnum(DayOfWeek.class, request.dayOfWeek(), "day of week"))
                .startTime(start)
                .endTime(end)
                .room(notBlank(request.room()) ? request.room().trim() : "Online")
                .term(course.getTerm())
                .weekType(notBlank(request.weekType()) ? parseEnum(WeekType.class, request.weekType(), "week type") : WeekType.ALL)
                .sessionLabel(notBlank(request.sessionLabel()) ? request.sessionLabel().trim() : "Live Session")
                .build();
        timetableEntryRepository.save(entry);
        return toTimetableResponse(entry);
    }

    @Transactional
    public void deleteTimetableEntry(Long id) {
        timetableEntryRepository.delete(timetableEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Timetable entry not found")));
    }

    private TimetableEntryResponse toTimetableResponse(TimetableEntry t) {
        Course c = t.getCourse();
        return new TimetableEntryResponse(
                t.getId(), c.getId(), c.getCode(), c.getTitle(), c.getColorHex(),
                t.getDayOfWeek().name(), t.getStartTime().format(HH_MM), t.getEndTime().format(HH_MM),
                t.getRoom(), t.getWeekType() != null ? t.getWeekType().name() : "ALL", t.getSessionLabel());
    }

    // ==================================================================== modules

    @Transactional(readOnly = true)
    public List<AdminDtos.ModuleResponse> listModules(Long courseId) {
        return courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::toModuleResponse)
                .toList();
    }

    @Transactional
    public AdminDtos.ModuleResponse createModule(AdminDtos.ModuleRequest request) {
        Course course = course(request.courseId());
        int next = courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).size();
        CourseModule module = CourseModule.builder()
                .course(course)
                .weekLabel(notBlank(request.weekLabel()) ? request.weekLabel().trim() : "Week " + (next + 1))
                .title(request.title().trim())
                .orderIndex(next)
                .build();
        courseModuleRepository.save(module);
        return toModuleResponse(module);
    }

    @Transactional
    public AdminDtos.ModuleResponse addModuleItem(Long moduleId, AdminDtos.ModuleItemRequest request) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        ModuleItem item = ModuleItem.builder()
                .module(module)
                .type(parseEnum(ModuleItemType.class, request.type(), "item type"))
                .title(request.title().trim())
                .dueAt(parseDateTime(request.dueAt(), "due date"))
                .points(request.points())
                .orderIndex(module.getItems().size())
                .build();
        module.getItems().add(item);
        courseModuleRepository.save(module);
        return toModuleResponse(module);
    }

    @Transactional
    public AdminDtos.ModuleResponse deleteModuleItem(Long moduleId, Long itemId) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        boolean removed = module.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new NotFoundException("Module item not found");
        }
        courseModuleRepository.save(module);
        return toModuleResponse(module);
    }

    @Transactional
    public void deleteModule(Long id) {
        courseModuleRepository.delete(courseModuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Module not found")));
    }

    private AdminDtos.ModuleResponse toModuleResponse(CourseModule m) {
        return new AdminDtos.ModuleResponse(
                m.getId(), m.getCourse().getId(), m.getWeekLabel(), m.getTitle(),
                m.getItems().stream()
                        .map(i -> new ModuleItemResponse(
                                i.getId(), i.getType().name(), i.getTitle(),
                                i.getDueAt() != null ? i.getDueAt().format(ISO_DT) : null,
                                i.getPoints()))
                        .toList());
    }

    // =================================================================== invoices

    @Transactional(readOnly = true)
    public List<AdminDtos.InvoiceResponse> listInvoices(Long studentId) {
        List<Invoice> rows = studentId != null
                ? invoiceRepository.findByStudentIdOrderByIssueDateDesc(studentId)
                : invoiceRepository.findAll().stream()
                    .sorted(Comparator.comparing(Invoice::getIssueDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        return rows.stream().map(this::toInvoiceResponse).toList();
    }

    @Transactional
    public AdminDtos.InvoiceResponse createInvoice(AdminDtos.InvoiceRequest request) {
        Student student = student(request.studentId());
        Invoice invoice = Invoice.builder()
                .student(student)
                .invoiceNumber(nextInvoiceNumber())
                .description(request.description().trim())
                .issueDate(LocalDate.now())
                .dueDate(parseDate(request.dueDate(), "due date"))
                .amount(request.amount())
                .status(InvoiceStatus.UNPAID)
                .build();
        invoiceRepository.save(invoice);
        return toInvoiceResponse(invoice);
    }

    @Transactional
    public AdminDtos.InvoiceResponse setInvoiceStatus(Long id, AdminDtos.InvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        invoice.setStatus(parseEnum(InvoiceStatus.class, request.status(), "invoice status"));
        invoiceRepository.save(invoice);
        return toInvoiceResponse(invoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        invoiceRepository.delete(invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found")));
    }

    private String nextInvoiceNumber() {
        long next = invoiceRepository.count() + 1;
        String candidate;
        do {
            candidate = String.format(Locale.ROOT, "INV-%d-%04d", LocalDate.now().getYear(), next++);
            final String c = candidate;
            if (invoiceRepository.findAll().stream().noneMatch(i -> c.equals(i.getInvoiceNumber()))) {
                return candidate;
            }
        } while (true);
    }

    private AdminDtos.InvoiceResponse toInvoiceResponse(Invoice i) {
        return new AdminDtos.InvoiceResponse(
                i.getId(), i.getStudent().getId(), i.getStudent().getFullName(), i.getInvoiceNumber(),
                i.getDescription(),
                i.getIssueDate() != null ? i.getIssueDate().toString() : null,
                i.getDueDate() != null ? i.getDueDate().toString() : null,
                i.getAmount(), i.getStatus().name());
    }

    // ======================================================== enrollment requests

    @Transactional(readOnly = true)
    public List<AdminDtos.EnrollmentRequestResponse> listEnrollmentRequests() {
        return enrollmentRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(this::toRequestResponse)
                .toList();
    }

    /** Approving a request enrolls the student in the course with the same code. */
    @Transactional
    public AdminDtos.EnrollmentRequestResponse decideEnrollmentRequest(Long id, boolean approve) {
        EnrollmentRequest request = enrollmentRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (request.getStatus() != EnrollmentRequestStatus.PENDING) {
            throw new BadRequestException("This request was already " + request.getStatus().name().toLowerCase(Locale.ROOT));
        }
        if (approve) {
            Course course = courseRepository.findByCodeIgnoreCase(request.getCatalogCourse().getCode())
                    .orElseThrow(() -> new BadRequestException(
                            "There's no course with the code " + request.getCatalogCourse().getCode()
                                    + " yet — create it under Courses first"));
            Student student = request.getStudent();
            if (enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).isEmpty()) {
                enrollmentRepository.save(Enrollment.builder().student(student).course(course).build());
            }
            request.setStatus(EnrollmentRequestStatus.APPROVED);
        } else {
            request.setStatus(EnrollmentRequestStatus.REJECTED);
        }
        enrollmentRequestRepository.save(request);
        return toRequestResponse(request);
    }

    private AdminDtos.EnrollmentRequestResponse toRequestResponse(EnrollmentRequest r) {
        return new AdminDtos.EnrollmentRequestResponse(
                r.getId(), r.getStudent().getId(), r.getStudent().getFullName(),
                r.getCatalogCourse().getCode(), r.getCatalogCourse().getTitle(),
                r.getRequestedAt() != null ? r.getRequestedAt().format(ISO_DT) : null,
                r.getStatus().name());
    }

    // ==================================================================== helpers

    private Student student(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    private Course course(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw, String label) {
        try {
            return Enum.valueOf(type, Objects.requireNonNull(raw).trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (RuntimeException ex) {
            throw new BadRequestException("Invalid " + label + ": " + raw);
        }
    }

    private static LocalDate parseDate(String raw, String label) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (RuntimeException ex) {
            throw new BadRequestException("Invalid " + label + " (expected YYYY-MM-DD): " + raw);
        }
    }

    /** Accepts "2026-10-01T23:59", "2026-10-01T23:59:00" or a bare date (→ end of day). */
    private static LocalDateTime parseDateTime(String raw, String label) {
        if (!notBlank(raw)) {
            return null;
        }
        String s = raw.trim();
        try {
            if (s.length() == 10) {
                return LocalDate.parse(s).atTime(23, 59);
            }
            return LocalDateTime.parse(s.length() == 16 ? s + ":00" : s);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid " + label + ": " + raw);
        }
    }

    private static LocalTime parseTime(String raw, String label) {
        try {
            return LocalTime.parse(raw.trim());
        } catch (RuntimeException ex) {
            throw new BadRequestException("Invalid " + label + " (expected HH:mm): " + raw);
        }
    }

    private static String initialsOf(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(p.charAt(0)));
        }
        return sb.toString();
    }
}
