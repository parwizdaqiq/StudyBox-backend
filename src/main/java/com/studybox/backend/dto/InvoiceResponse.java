package com.studybox.backend.dto;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String description,
        String issueDate,
        String dueDate,
        double amount,
        String status
) {
}
