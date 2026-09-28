package com.studybox.backend.dto;

public record ConversationSummaryResponse(
        Long id,
        String subject,
        String withName,
        String courseTitle,
        String lastMessagePreview,
        String lastMessageAt,
        boolean unread
) {
}
