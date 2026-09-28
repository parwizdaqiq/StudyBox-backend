package com.studybox.backend.repository;

import com.studybox.backend.domain.Educator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducatorRepository extends JpaRepository<Educator, Long> {
}
