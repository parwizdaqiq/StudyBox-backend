package com.studybox.backend.service;

import com.studybox.backend.domain.Assignment;
import com.studybox.backend.domain.Course;
import com.studybox.backend.domain.TimetableEntry;
import com.studybox.backend.dto.CalendarEventResponse;
import com.studybox.backend.repository.AssignmentRepository;
import com.studybox.backend.repository.TimetableEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Merges assignment deadlines and the recurring weekly timetable into one chronological
 *  feed — Classter/Canvas both surface a combined calendar like this. */
@Service
public class CalendarService {

    private final CourseService courseService;
    private final AssignmentRepository assignmentRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public CalendarService(CourseService courseService, AssignmentRepository assignmentRepository, TimetableEntryRepository timetableEntryRepository) {
        this.courseService = courseService;
        this.assignmentRepository = assignmentRepository;
        this.timetableEntryRepository = timetableEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> upcoming(Long studentId, int daysAhead) {
        List<Long> courseIds = courseService.enrolledCourseIds(studentId);
        List<CalendarEventResponse> events = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(daysAhead);
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (Assignment a : assignmentRepository.findByCourseIdIn(courseIds)) {
            if (a.getDueAt() == null) continue;
            LocalDate dueDate = a.getDueAt().toLocalDate();
            if (dueDate.isBefore(today) || dueDate.isAfter(horizon)) continue;
            Course course = a.getCourse();
            events.add(new CalendarEventResponse(
                    dueDate.format(dateFmt),
                    a.getDueAt().format(timeFmt),
                    a.getTitle() + " due",
                    "Assignment",
                    course.getId(), course.getTitle(), course.getColorHex()));
        }

        for (TimetableEntry entry : timetableEntryRepository.findByCourseIdIn(courseIds)) {
            for (int offset = 0; offset <= daysAhead; offset++) {
                LocalDate date = today.plusDays(offset);
                if (date.getDayOfWeek() != entry.getDayOfWeek()) continue;
                Course course = entry.getCourse();
                events.add(new CalendarEventResponse(
                        date.format(dateFmt),
                        entry.getStartTime().format(timeFmt),
                        (entry.getSessionLabel() != null ? entry.getSessionLabel() : "Class") + " — " + course.getTitle(),
                        "Class",
                        course.getId(), course.getTitle(), course.getColorHex()));
            }
        }

        return events.stream()
                .sorted(Comparator.comparing(CalendarEventResponse::date).thenComparing(CalendarEventResponse::time))
                .toList();
    }
}
