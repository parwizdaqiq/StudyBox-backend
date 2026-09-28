package com.studybox.backend.service;

import com.studybox.backend.domain.DiscussionPost;
import com.studybox.backend.domain.DiscussionThread;
import com.studybox.backend.dto.*;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.CourseRepository;
import com.studybox.backend.repository.DiscussionPostRepository;
import com.studybox.backend.repository.DiscussionThreadRepository;
import com.studybox.backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class DiscussionService {

    private final DiscussionThreadRepository discussionThreadRepository;
    private final DiscussionPostRepository discussionPostRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DiscussionService(
            DiscussionThreadRepository discussionThreadRepository,
            DiscussionPostRepository discussionPostRepository,
            EnrollmentRepository enrollmentRepository) {
        this.discussionThreadRepository = discussionThreadRepository;
        this.discussionPostRepository = discussionPostRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DiscussionThreadSummaryResponse> list(Long studentId, Long courseId) {
        List<Long> courseIds = courseId != null
                ? List.of(courseId)
                : enrollmentRepository.findByStudentId(studentId).stream().map(e -> e.getCourse().getId()).toList();

        return discussionThreadRepository.findByCourseIdIn(courseIds).stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(DiscussionThreadSummaryResponse::createdAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public DiscussionThreadDetailResponse detail(Long threadId) {
        DiscussionThread thread = discussionThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
        return toDetail(thread);
    }

    @Transactional
    public DiscussionThreadDetailResponse addPost(Long studentId, Long threadId, String body, String authorName) {
        DiscussionThread thread = discussionThreadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
        if (thread.isClosedForComments()) {
            throw new BadRequestException("This discussion is closed for comments");
        }
        DiscussionPost post = DiscussionPost.builder()
                .thread(thread)
                .authorName(authorName)
                .body(body)
                .postedAt(LocalDateTime.now())
                .build();
        discussionPostRepository.save(post);
        thread.getPosts().add(post);
        return toDetail(thread);
    }

    /** Starts a new thread (with its first post) in a course the student is enrolled in. */
    @Transactional
    public DiscussionThreadDetailResponse createThread(Long studentId, Long courseId, String title, String body, String authorName) {
        var enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new BadRequestException("You can only start discussions in your own courses"));
        LocalDateTime now = LocalDateTime.now();
        DiscussionThread thread = DiscussionThread.builder()
                .course(enrollment.getCourse())
                .title(title.trim())
                .authorName(authorName)
                .createdAt(now)
                .build();
        discussionThreadRepository.save(thread);
        DiscussionPost post = DiscussionPost.builder()
                .thread(thread)
                .authorName(authorName)
                .body(body.trim())
                .postedAt(now)
                .build();
        discussionPostRepository.save(post);
        thread.getPosts().add(post);
        return toDetail(thread);
    }

    private DiscussionThreadSummaryResponse toSummary(DiscussionThread t) {
        return new DiscussionThreadSummaryResponse(
                t.getId(), t.getCourse().getId(), t.getCourse().getTitle(), t.getTitle(), t.getAuthorName(),
                t.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), t.isClosedForComments(), t.getPosts().size());
    }

    private DiscussionThreadDetailResponse toDetail(DiscussionThread t) {
        List<DiscussionPostResponse> posts = t.getPosts().stream()
                .sorted(Comparator.comparing(DiscussionPost::getPostedAt))
                .map(p -> new DiscussionPostResponse(p.getId(), p.getAuthorName(), p.getBody(),
                        p.getPostedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .toList();
        return new DiscussionThreadDetailResponse(
                t.getId(), t.getCourse().getId(), t.getCourse().getTitle(), t.getTitle(), t.getAuthorName(),
                t.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), t.isClosedForComments(), posts);
    }
}
