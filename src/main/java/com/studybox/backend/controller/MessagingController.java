package com.studybox.backend.controller;

import com.studybox.backend.dto.ConversationDetailResponse;
import com.studybox.backend.dto.ConversationSummaryResponse;
import com.studybox.backend.dto.MessageReplyRequest;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    public List<ConversationSummaryResponse> inbox(@AuthenticationPrincipal CurrentUser user) {
        return messagingService.inbox(user.studentId());
    }

    @GetMapping("/{id}")
    public ConversationDetailResponse thread(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return messagingService.thread(user.studentId(), id);
    }

    @PostMapping("/{id}/reply")
    public ConversationDetailResponse reply(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @Valid @RequestBody MessageReplyRequest request) {
        return messagingService.reply(user.studentId(), id, request.body());
    }
}
