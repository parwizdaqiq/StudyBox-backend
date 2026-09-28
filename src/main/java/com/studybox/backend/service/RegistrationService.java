package com.studybox.backend.service;

import com.studybox.backend.domain.CatalogCourse;
import com.studybox.backend.domain.EnrollmentRequest;
import com.studybox.backend.dto.CatalogCourseResponse;
import com.studybox.backend.dto.EnrollmentRequestResponse;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.CatalogCourseRepository;
import com.studybox.backend.repository.EnrollmentRequestRepository;
import com.studybox.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistrationService {

    private final CatalogCourseRepository catalogCourseRepository;
    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final StudentRepository studentRepository;

    public RegistrationService(
            CatalogCourseRepository catalogCourseRepository,
            EnrollmentRequestRepository enrollmentRequestRepository,
            StudentRepository studentRepository) {
        this.catalogCourseRepository = catalogCourseRepository;
        this.enrollmentRequestRepository = enrollmentRequestRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogCourseResponse> catalog() {
        return catalogCourseRepository.findAll().stream()
                .map(this::toCatalogResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRequestResponse> myRequests(Long studentId) {
        return enrollmentRequestRepository.findByStudentIdOrderByRequestedAtDesc(studentId).stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional
    public EnrollmentRequestResponse submitRequest(Long studentId, Long catalogCourseId) {
        CatalogCourse catalogCourse = catalogCourseRepository.findById(catalogCourseId)
                .orElseThrow(() -> new NotFoundException("Course not found in catalog"));
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        EnrollmentRequest request = EnrollmentRequest.builder()
                .student(student)
                .catalogCourse(catalogCourse)
                .build();
        enrollmentRequestRepository.save(request);
        return toRequestResponse(request);
    }

    private CatalogCourseResponse toCatalogResponse(CatalogCourse c) {
        return new CatalogCourseResponse(c.getId(), c.getCode(), c.getTitle(), c.getCredits(), c.getTerm(), c.getCategory(), c.getDescription());
    }

    private EnrollmentRequestResponse toRequestResponse(EnrollmentRequest r) {
        return new EnrollmentRequestResponse(
                r.getId(),
                r.getCatalogCourse().getId(),
                r.getCatalogCourse().getCode(),
                r.getCatalogCourse().getTitle(),
                r.getRequestedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                r.getStatus().name());
    }
}
