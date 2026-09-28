package com.studybox.backend.service;

import com.studybox.backend.domain.Conversation;
import com.studybox.backend.domain.Message;
import com.studybox.backend.dto.ConversationDetailResponse;
import com.studybox.backend.dto.ConversationSummaryResponse;
import com.studybox.backend.dto.MessageResponse;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.ConversationRepository;
import com.studybox.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessagingService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> inbox(Long studentId) {
        return conversationRepository.findByStudentId(studentId).stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(ConversationSummaryResponse::lastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public ConversationDetailResponse thread(Long studentId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertOwnership(conversation, studentId);

        conversation.getMessages().forEach(m -> m.setRead(true));

        return toDetail(conversation);
    }

    @Transactional
    public ConversationDetailResponse reply(Long studentId, Long conversationId, String body) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        assertOwnership(conversation, studentId);

        Message message = Message.builder()
                .conversation(conversation)
                .senderName(conversation.getStudent().getFullName())
                .fromStudent(true)
                .body(body)
                .sentAt(LocalDateTime.now())
                .read(true)
                .build();
        messageRepository.save(message);
        conversation.getMessages().add(message);

        return toDetail(conversation);
    }

    private void assertOwnership(Conversation conversation, Long studentId) {
        if (!conversation.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This conversation does not belong to you");
        }
    }

    private ConversationSummaryResponse toSummary(Conversation c) {
        Message last = c.getMessages().stream()
                .max(Comparator.comparing(Message::getSentAt))
                .orElse(null);
        boolean unread = c.getMessages().stream().anyMatch(m -> !m.isRead() && !m.isFromStudent());
        return new ConversationSummaryResponse(
                c.getId(),
                c.getSubject(),
                c.getWithName(),
                c.getCourse() != null ? c.getCourse().getTitle() : null,
                last != null ? truncate(last.getBody(), 90) : null,
                last != null ? last.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                unread);
    }

    private ConversationDetailResponse toDetail(Conversation c) {
        List<MessageResponse> messages = c.getMessages().stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .map(m -> new MessageResponse(
                        m.getId(), m.getSenderName(), m.isFromStudent(),
                        m.getBody(), m.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .toList();
        return new ConversationDetailResponse(
                c.getId(), c.getSubject(), c.getWithName(),
                c.getCourse() != null ? c.getCourse().getTitle() : null, messages);
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }
}
