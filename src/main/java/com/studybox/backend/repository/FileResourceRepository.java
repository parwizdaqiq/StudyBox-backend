package com.studybox.backend.repository;

import com.studybox.backend.domain.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    List<FileResource> findByStudentIdOrderByUploadedAtDesc(Long studentId);
    List<FileResource> findByStudentIdAndCourseIdOrderByUploadedAtDesc(Long studentId, Long courseId);
}
