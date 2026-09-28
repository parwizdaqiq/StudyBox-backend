package com.studybox.backend.repository;

import com.studybox.backend.domain.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByStudentIdOrderByInstallmentNumberAsc(Long studentId);
}
