package com.studybox.backend.controller;

import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.TranscriptService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TranscriptService transcriptService;

    public ReportController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @GetMapping("/transcript")
    public ResponseEntity<byte[]> transcript(@AuthenticationPrincipal CurrentUser user) {
        byte[] pdf = transcriptService.generateUnofficialTranscript(user.studentId());
        ContentDisposition disposition = ContentDisposition.inline().filename("studybox-unofficial-transcript.pdf").build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }
}
