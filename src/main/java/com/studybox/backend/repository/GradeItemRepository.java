package com.studybox.backend.repository;

import com.studybox.backend.domain.GradeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeItemRepository extends JpaRepository<GradeItem, Long> {
    List<GradeItem> findByCategoryCourseIdAndStudentId(Long courseId, Long studentId);
    List<GradeItem> findByStudentId(Long studentId);
}
