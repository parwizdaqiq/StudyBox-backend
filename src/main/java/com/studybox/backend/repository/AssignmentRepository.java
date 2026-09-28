package com.studybox.backend.repository;

import com.studybox.backend.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdIn(List<Long> courseIds);
    List<Assignment> findByCourseId(Long courseId);
}
