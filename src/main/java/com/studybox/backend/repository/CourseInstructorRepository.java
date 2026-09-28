package com.studybox.backend.repository;

import com.studybox.backend.domain.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, Long> {
    List<CourseInstructor> findByCourseId(Long courseId);
    List<CourseInstructor> findByCourseIdIn(List<Long> courseIds);
}
