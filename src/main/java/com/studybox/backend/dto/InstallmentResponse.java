package com.studybox.backend.dto;

public record InstallmentResponse(
        Long id,
        String term,
        int installmentNumber,
        String dueDate,
        double amount,
        String status
) {
}
