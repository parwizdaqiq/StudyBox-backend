package com.studybox.backend.dto;

public record AnnouncementResponse(
        Long id,
        String courseTitle,
        String title,
        String body,
        String postedAt,
        String postedBy
) {
}
