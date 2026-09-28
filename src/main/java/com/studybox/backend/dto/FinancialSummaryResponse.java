package com.studybox.backend.dto;

public record FinancialSummaryResponse(
        String currency,
        double agreedAmount,
        double periodPayment,
        double periodBalance,
        double periodOutstanding,
        double totalBalance
) {
}
