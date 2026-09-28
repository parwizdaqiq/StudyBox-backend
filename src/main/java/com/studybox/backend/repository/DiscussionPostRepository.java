package com.studybox.backend.repository;

import com.studybox.backend.domain.DiscussionPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {
}
