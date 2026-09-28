package com.studybox.backend.repository;

import com.studybox.backend.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByStudentId(Long studentId);
}
