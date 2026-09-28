package com.studybox.backend.repository;

import com.studybox.backend.domain.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByStudentIdOrderBySessionDateDesc(Long studentId);
    List<ClassSession> findByStudentIdAndCourseIdOrderBySessionDateDesc(Long studentId, Long courseId);
}
