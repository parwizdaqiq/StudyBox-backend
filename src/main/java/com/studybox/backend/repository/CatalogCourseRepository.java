package com.studybox.backend.repository;

import java.util.Optional;
import com.studybox.backend.domain.CatalogCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogCourseRepository extends JpaRepository<CatalogCourse, Long> {

    Optional<CatalogCourse> findByCodeIgnoreCase(String code);

}
