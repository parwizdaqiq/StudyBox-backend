package com.studybox.backend.repository;

import com.studybox.backend.domain.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByCourseIdIn(List<Long> courseIds);
}
