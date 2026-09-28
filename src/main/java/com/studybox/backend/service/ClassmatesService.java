package com.studybox.backend.service;

import com.studybox.backend.domain.Enrollment;
import com.studybox.backend.dto.ClassmateResponse;
import com.studybox.backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ClassmatesService {

    private final EnrollmentRepository enrollmentRepository;

    public ClassmatesService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassmateResponse> forStudent(Long studentId, Long courseId) {
        List<Long> courseIds = courseId != null
                ? List.of(courseId)
                : enrollmentRepository.findByStudentId(studentId).stream().map(e -> e.getCourse().getId()).toList();

        return enrollmentRepository.findByCourseIdIn(courseIds).stream()
                .filter(e -> !e.getStudent().getId().equals(studentId))
                .map(this::toResponse)
                .sorted(Comparator.comparing(ClassmateResponse::fullName))
                .toList();
    }

    private ClassmateResponse toResponse(Enrollment e) {
        String name = e.getStudent().getFullName();
        return new ClassmateResponse(
                e.getStudent().getId(),
                name,
                initialsOf(name),
                e.getStudent().getProgramme(),
                e.getCourse().getId(),
                e.getCourse().getTitle());
    }

    private String initialsOf(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.toString();
    }
}
