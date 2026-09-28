package com.studybox.backend.repository;

import com.studybox.backend.domain.EnrollmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, Long> {
    List<EnrollmentRequest> findByStudentIdOrderByRequestedAtDesc(Long studentId);

    List<EnrollmentRequest> findAllByOrderByRequestedAtDesc();

}
