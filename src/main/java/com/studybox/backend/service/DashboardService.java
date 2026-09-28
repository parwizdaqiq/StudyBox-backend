package com.studybox.backend.service;

import com.studybox.backend.domain.Announcement;
import com.studybox.backend.domain.AssignmentStatus;
import com.studybox.backend.domain.Enrollment;
import com.studybox.backend.domain.Student;
import com.studybox.backend.dto.*;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.AnnouncementRepository;
import com.studybox.backend.repository.AssignmentRepository;
import com.studybox.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;
    private final GradeService gradeService;
    private final TimetableService timetableService;
    private final AnnouncementRepository announcementRepository;

    public DashboardService(
            StudentRepository studentRepository,
            CourseService courseService,
            AssignmentService assignmentService,
            AssignmentRepository assignmentRepository,
            GradeService gradeService,
            TimetableService timetableService,
            AnnouncementRepository announcementRepository) {
        this.studentRepository = studentRepository;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
        this.gradeService = gradeService;
        this.timetableService = timetableService;
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse build(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        List<Long> courseIds = courseService.enrolledCourseIds(studentId);
        List<Enrollment> enrollments = courseService.enrollments(studentId);

        int activeCourses = courseIds.size();

        long pendingAssignments = assignmentRepository.findByCourseIdIn(courseIds).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.NOT_STARTED || a.getStatus() == AssignmentStatus.MISSING)
                .count();

        double attendanceRate = computeAttendanceRate(enrollments);

        List<GradeCourseResponse> grades = gradeService.listForStudent(studentId);
        java.util.OptionalDouble avgOpt = grades.stream()
                .map(GradeCourseResponse::totalPercent)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        Double averageGrade = avgOpt.isPresent() ? round1(avgOpt.getAsDouble()) : null;

        List<TimetableEntryResponse> today = timetableService.today(studentId);

        List<AssignmentResponse> upcoming = assignmentService.listForStudent(studentId, null, null).stream()
                .filter(a -> a.dueAt() != null)
                .filter(a -> !"GRADED".equals(a.status()))
                .limit(5)
                .toList();

        List<AnnouncementResponse> announcements = announcementsFor(courseIds).stream()
                .limit(6)
                .toList();

        return new DashboardResponse(
                AuthService.toProfile(student),
                new DashboardStatsResponse(activeCourses, (int) pendingAssignments, round1(attendanceRate), averageGrade),
                today,
                upcoming,
                announcements,
                quickActions());
    }

    private double computeAttendanceRate(List<Enrollment> enrollments) {
        int presences = 0;
        int totalSessions = 0;
        for (Enrollment e : enrollments) {
            presences += e.getPresences();
            totalSessions += e.getPresences() + e.getUnauthorizedAbsences() + e.getAuthorizedAbsences() + e.getLates();
        }
        if (totalSessions == 0) {
            return 100.0;
        }
        return (presences * 100.0) / totalSessions;
    }

    /** Every announcement this student should see: programme-wide ones plus those posted to
     *  any course they're enrolled in, newest first. */
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> announcementsForStudent(Long studentId) {
        return announcementsFor(courseService.enrolledCourseIds(studentId));
    }

    private List<AnnouncementResponse> announcementsFor(List<Long> courseIds) {
        List<Announcement> rows = courseIds.isEmpty()
                ? announcementRepository.findByCourseIsNullOrderByPostedAtDesc()
                : announcementRepository.findVisibleTo(courseIds);
        return rows.stream().map(this::toAnnouncementResponse).toList();
    }

    private AnnouncementResponse toAnnouncementResponse(Announcement a) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new AnnouncementResponse(
                a.getId(),
                a.getCourse() != null ? a.getCourse().getTitle() : "StudyBox",
                a.getTitle(),
                a.getBody(),
                a.getPostedAt() != null ? a.getPostedAt().format(fmt) : null,
                a.getPostedBy());
    }

    private List<QuickActionResponse> quickActions() {
        return List.of(
                new QuickActionResponse("My Courses", "school", null),
                new QuickActionResponse("Grades", "fact_check", null),
                new QuickActionResponse("Message Center", "mail", null),
                new QuickActionResponse("Files", "folder", null),
                new QuickActionResponse("Classmates", "groups", null));
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
