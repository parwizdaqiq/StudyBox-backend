package com.studybox.backend.service;

import com.studybox.backend.domain.Course;
import com.studybox.backend.domain.GradeCategory;
import com.studybox.backend.domain.GradeItem;
import com.studybox.backend.dto.GradeCategoryResponse;
import com.studybox.backend.dto.GradeCourseResponse;
import com.studybox.backend.dto.GradeItemResponse;
import com.studybox.backend.repository.GradeCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeCategoryRepository gradeCategoryRepository;
    private final CourseService courseService;

    public GradeService(GradeCategoryRepository gradeCategoryRepository, CourseService courseService) {
        this.gradeCategoryRepository = gradeCategoryRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<GradeCourseResponse> listForStudent(Long studentId) {
        List<Long> courseIds = courseService.enrolledCourseIds(studentId);
        List<GradeCategory> categories = gradeCategoryRepository.findByCourseIdInOrderByOrderIndexAsc(courseIds);

        Map<Long, List<GradeCategory>> byCourse = categories.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCourse().getId(),
                        java.util.LinkedHashMap::new,
                        Collectors.toList()));

        return byCourse.values().stream()
                .map(list -> toCourseResponse(list, studentId))
                .sorted(Comparator.comparing(GradeCourseResponse::courseCode))
                .toList();
    }

    /** Same as {@link #listForStudent}, but scoped to one course — used by the Super Admin
     *  panel to show the recomputed total right after publishing a grade. */
    @Transactional(readOnly = true)
    public GradeCourseResponse courseForStudent(Long studentId, Long courseId) {
        List<GradeCategory> categories = gradeCategoryRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        if (categories.isEmpty()) {
            return null;
        }
        return toCourseResponse(categories, studentId);
    }

    /** An item is visible to this student if it's shared (no owner) or owned by them. */
    private boolean visibleTo(GradeItem item, Long studentId) {
        return item.getStudent() == null || item.getStudent().getId().equals(studentId);
    }

    private GradeCourseResponse toCourseResponse(List<GradeCategory> categories, Long studentId) {
        Course course = categories.get(0).getCourse();

        double weightedSum = 0;
        double weightConsidered = 0;

        List<GradeCategoryResponse> categoryResponses = categories.stream()
                .map(cat -> {
                    Double categoryPercent = computeCategoryPercent(cat, studentId);
                    return new GradeCategoryResponse(
                            cat.getName(),
                            cat.getWeightPercent(),
                            toItemResponses(cat, studentId),
                            categoryPercent);
                })
                .toList();

        for (int i = 0; i < categories.size(); i++) {
            Double pct = categoryResponses.get(i).categoryPercent();
            if (pct != null) {
                weightedSum += pct * categories.get(i).getWeightPercent() / 100.0;
                weightConsidered += categories.get(i).getWeightPercent();
            }
        }

        Double totalPercent = weightConsidered > 0 ? round1(weightedSum) : null;

        return new GradeCourseResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getColorHex(),
                categoryResponses,
                totalPercent);
    }

    private Double computeCategoryPercent(GradeCategory category, Long studentId) {
        double earned = 0;
        double possible = 0;
        boolean anyPosted = false;
        for (GradeItem item : category.getItems()) {
            if (!visibleTo(item, studentId)) continue;
            if (item.isPosted() && item.getScoreEarned() != null) {
                earned += item.getScoreEarned();
                possible += item.getMaxScore();
                anyPosted = true;
            }
        }
        if (!anyPosted || possible == 0) {
            return null;
        }
        return round1((earned / possible) * 100.0);
    }

    private List<GradeItemResponse> toItemResponses(GradeCategory category, Long studentId) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return category.getItems().stream()
                .filter(item -> visibleTo(item, studentId))
                .map(item -> new GradeItemResponse(
                        item.getId(),
                        item.getName(),
                        item.getMaxScore(),
                        item.getScoreEarned(),
                        item.isPosted(),
                        item.getAssignment() != null && item.getAssignment().getDueAt() != null
                                ? item.getAssignment().getDueAt().format(fmt)
                                : null))
                .toList();
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
