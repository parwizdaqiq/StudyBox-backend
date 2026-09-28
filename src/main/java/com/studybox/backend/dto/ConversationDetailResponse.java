package com.studybox.backend.dto;

import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String subject,
        String withName,
        String courseTitle,
        List<MessageResponse> messages
) {
}
