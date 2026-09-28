package com.studybox.backend.dto;

import java.util.List;

public record DiscussionThreadDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String authorName,
        String createdAt,
        boolean closedForComments,
        List<DiscussionPostResponse> posts
) {
}
