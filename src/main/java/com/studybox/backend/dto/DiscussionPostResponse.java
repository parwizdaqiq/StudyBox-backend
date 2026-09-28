package com.studybox.backend.dto;

public record DiscussionPostResponse(
        Long id,
        String authorName,
        String body,
        String postedAt
) {
}
