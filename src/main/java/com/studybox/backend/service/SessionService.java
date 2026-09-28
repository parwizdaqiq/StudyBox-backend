package com.studybox.backend.service;

import com.studybox.backend.domain.ClassSession;
import com.studybox.backend.dto.SessionResponse;
import com.studybox.backend.repository.ClassSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SessionService {

    private final ClassSessionRepository classSessionRepository;

    public SessionService(ClassSessionRepository classSessionRepository) {
        this.classSessionRepository = classSessionRepository;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> list(Long studentId, Long courseId, LocalDate from, LocalDate to, Boolean hasFiles) {
        List<ClassSession> sessions = courseId != null
                ? classSessionRepository.findByStudentIdAndCourseIdOrderBySessionDateDesc(studentId, courseId)
                : classSessionRepository.findByStudentIdOrderBySessionDateDesc(studentId);

        return sessions.stream()
                .filter(s -> from == null || !s.getSessionDate().isBefore(from))
                .filter(s -> to == null || !s.getSessionDate().isAfter(to))
                .filter(s -> hasFiles == null || s.isHasFiles() == hasFiles)
                .map(this::toResponse)
                .toList();
    }

    private SessionResponse toResponse(ClassSession s) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        return new SessionResponse(
                s.getId(),
                s.getCourse().getId(),
                s.getCourse().getCode(),
                s.getCourse().getTitle(),
                s.getCourse().getColorHex(),
                s.getSessionDate().format(dateFmt),
                s.getSessionLabel(),
                s.getStatus().name(),
                s.isHasFiles());
    }
}
