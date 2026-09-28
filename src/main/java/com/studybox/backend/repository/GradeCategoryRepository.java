package com.studybox.backend.repository;

import com.studybox.backend.domain.GradeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeCategoryRepository extends JpaRepository<GradeCategory, Long> {
    List<GradeCategory> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    List<GradeCategory> findByCourseIdInOrderByOrderIndexAsc(List<Long> courseIds);
}
