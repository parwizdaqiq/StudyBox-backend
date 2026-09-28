package com.studybox.backend.dto;

public record DiscussionThreadSummaryResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String authorName,
        String createdAt,
        boolean closedForComments,
        int replyCount
) {
}
