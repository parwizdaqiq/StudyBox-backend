package com.studybox.backend.controller;

import com.studybox.backend.dto.FinancialSummaryResponse;
import com.studybox.backend.dto.InstallmentResponse;
import com.studybox.backend.dto.InvoiceResponse;
import com.studybox.backend.security.CurrentUser;
import com.studybox.backend.service.FinancialService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial")
public class FinancialController {

    private final FinancialService financialService;

    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping("/summary")
    public FinancialSummaryResponse summary(@AuthenticationPrincipal CurrentUser user) {
        return financialService.summary(user.studentId());
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices(@AuthenticationPrincipal CurrentUser user) {
        return financialService.invoices(user.studentId());
    }

    @GetMapping("/installments")
    public List<InstallmentResponse> installments(@AuthenticationPrincipal CurrentUser user) {
        return financialService.installments(user.studentId());
    }

    // Invoice status (paid / unpaid) is set by the Super Admin — see AdminContentController.
}
