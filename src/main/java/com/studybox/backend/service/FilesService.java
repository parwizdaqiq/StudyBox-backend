package com.studybox.backend.service;

import com.studybox.backend.domain.FileResource;
import com.studybox.backend.dto.FileResourceResponse;
import com.studybox.backend.repository.FileResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FilesService {

    private final FileResourceRepository fileResourceRepository;

    public FilesService(FileResourceRepository fileResourceRepository) {
        this.fileResourceRepository = fileResourceRepository;
    }

    @Transactional(readOnly = true)
    public List<FileResourceResponse> list(Long studentId, Long courseId) {
        List<FileResource> files = courseId != null
                ? fileResourceRepository.findByStudentIdAndCourseIdOrderByUploadedAtDesc(studentId, courseId)
                : fileResourceRepository.findByStudentIdOrderByUploadedAtDesc(studentId);
        return files.stream().map(this::toResponse).toList();
    }

    private FileResourceResponse toResponse(FileResource f) {
        return new FileResourceResponse(
                f.getId(),
                f.getFileName(),
                f.getFileType(),
                f.getSizeLabel(),
                f.getCategory().name(),
                f.getCourse() != null ? f.getCourse().getId() : null,
                f.getCourse() != null ? f.getCourse().getTitle() : null,
                f.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
