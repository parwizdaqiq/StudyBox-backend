package com.studybox.backend.service;

import com.studybox.backend.domain.Assignment;
import com.studybox.backend.domain.Course;
import com.studybox.backend.dto.AssignmentResponse;
import com.studybox.backend.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;

    public AssignmentService(AssignmentRepository assignmentRepository, CourseService courseService) {
        this.assignmentRepository = assignmentRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> listForStudent(Long studentId, Long courseIdFilter, String statusFilter) {
        List<Long> courseIds = courseService.enrolledCourseIds(studentId);
        if (courseIdFilter != null) {
            courseIds = courseIds.stream().filter(id -> id.equals(courseIdFilter)).toList();
        }

        List<Assignment> assignments = assignmentRepository.findByCourseIdIn(courseIds);

        return assignments.stream()
                .filter(a -> statusFilter == null || statusFilter.isBlank()
                        || a.getStatus().name().equalsIgnoreCase(statusFilter))
                .map(this::toResponse)
                .sorted(Comparator.comparing(
                        AssignmentResponse::dueAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private AssignmentResponse toResponse(Assignment a) {
        Course course = a.getCourse();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new AssignmentResponse(
                a.getId(),
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getColorHex(),
                a.getTitle(),
                a.getType().name(),
                a.getCategoryLabel(),
                a.getAvailableFrom() != null ? a.getAvailableFrom().format(fmt) : null,
                a.getDueAt() != null ? a.getDueAt().format(fmt) : null,
                a.getPointsPossible(),
                a.getScoreEarned(),
                a.getStatus().name());
    }
}
