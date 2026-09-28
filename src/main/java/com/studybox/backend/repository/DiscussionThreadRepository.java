package com.studybox.backend.repository;

import com.studybox.backend.domain.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, Long> {
    List<DiscussionThread> findByCourseIdIn(List<Long> courseIds);
    List<DiscussionThread> findByCourseId(Long courseId);
}
