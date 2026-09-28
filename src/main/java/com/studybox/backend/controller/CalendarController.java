package com.studybox.backend.controller;

import com.studybox.backend.dto.CalendarEventResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.CalendarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public List<CalendarEventResponse> upcoming(@AuthenticationPrincipal CurrentUser user, @RequestParam(defaultValue = "21") int daysAhead) {
        return calendarService.upcoming(user.studentId(), daysAhead);
    }
}
