package com.studybox.backend.service;

import com.studybox.backend.domain.Course;
import com.studybox.backend.domain.TimetableEntry;
import com.studybox.backend.dto.TimetableEntryResponse;
import com.studybox.backend.repository.TimetableEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class TimetableService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final CourseService courseService;

    public TimetableService(TimetableEntryRepository timetableEntryRepository, CourseService courseService) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> weekFor(Long studentId) {
        List<Long> courseIds = courseService.enrolledCourseIds(studentId);
        List<TimetableEntry> entries = timetableEntryRepository.findByCourseIdIn(courseIds);

        return entries.stream()
                .map(this::toResponse)
                .sorted(Comparator
                        .comparing((TimetableEntryResponse r) -> DayOfWeek.valueOf(r.dayOfWeek()))
                        .thenComparing(TimetableEntryResponse::startTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> today(Long studentId) {
        DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        return weekFor(studentId).stream()
                .filter(e -> e.dayOfWeek().equals(today.name()))
                .toList();
    }

    private TimetableEntryResponse toResponse(TimetableEntry entry) {
        Course course = entry.getCourse();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        return new TimetableEntryResponse(
                entry.getId(),
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getColorHex(),
                entry.getDayOfWeek().name(),
                entry.getStartTime().format(timeFmt),
                entry.getEndTime().format(timeFmt),
                entry.getRoom(),
                entry.getWeekType().name(),
                entry.getSessionLabel());
    }
}
