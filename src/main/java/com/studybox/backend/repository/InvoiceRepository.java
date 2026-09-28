package com.studybox.backend.repository;

import com.studybox.backend.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStudentIdOrderByIssueDateDesc(Long studentId);
}
