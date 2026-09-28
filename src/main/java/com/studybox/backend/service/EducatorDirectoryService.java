package com.studybox.backend.service;

import com.studybox.backend.domain.CourseInstructor;
import com.studybox.backend.domain.Educator;
import com.studybox.backend.dto.InstructorResponse;
import com.studybox.backend.repository.CourseInstructorRepository;
import com.studybox.backend.repository.EducatorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EducatorDirectoryService {

    private final EducatorRepository educatorRepository;
    private final CourseInstructorRepository courseInstructorRepository;

    public EducatorDirectoryService(EducatorRepository educatorRepository, CourseInstructorRepository courseInstructorRepository) {
        this.educatorRepository = educatorRepository;
        this.courseInstructorRepository = courseInstructorRepository;
    }

    @Transactional(readOnly = true)
    public List<InstructorResponse> all() {
        List<Educator> educators = educatorRepository.findAll();
        Map<Long, List<CourseInstructor>> byEducator = courseInstructorRepository.findAll().stream()
                .collect(Collectors.groupingBy(ci -> ci.getEducator().getId()));

        return educators.stream()
                .map(e -> new InstructorResponse(
                        e.getId(),
                        e.getFullName(),
                        e.getTitle(),
                        e.getEmail(),
                        e.getAvatarInitials(),
                        byEducator.getOrDefault(e.getId(), List.of()).stream()
                                .map(ci -> ci.getCourse().getTitle())
                                .toList()))
                .sorted((a, b) -> a.fullName().compareTo(b.fullName()))
                .toList();
    }
}
