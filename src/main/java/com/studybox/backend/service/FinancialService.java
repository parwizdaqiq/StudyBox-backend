package com.studybox.backend.service;

import com.studybox.backend.domain.Installment;
import com.studybox.backend.domain.Invoice;
import com.studybox.backend.domain.InvoiceStatus;
import com.studybox.backend.dto.FinancialSummaryResponse;
import com.studybox.backend.dto.InstallmentResponse;
import com.studybox.backend.dto.InvoiceResponse;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.InstallmentRepository;
import com.studybox.backend.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FinancialService {

    private final InvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;

    public FinancialService(InvoiceRepository invoiceRepository, InstallmentRepository installmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse summary(Long studentId) {
        List<Invoice> invoices = invoiceRepository.findByStudentIdOrderByIssueDateDesc(studentId);
        double agreedAmount = invoices.stream().mapToDouble(Invoice::getAmount).sum();
        double outstanding = invoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID)
                .mapToDouble(Invoice::getAmount).sum();
        double paid = agreedAmount - outstanding;

        List<Installment> installments = installmentRepository.findByStudentIdOrderByInstallmentNumberAsc(studentId);
        double periodPayment = installments.stream()
                .filter(i -> i.getStatus() == com.studybox.backend.domain.InstallmentStatus.UNPAID)
                .findFirst()
                .map(Installment::getAmount)
                .orElse(0.0);

        return new FinancialSummaryResponse(
                "EUR",
                round2(agreedAmount),
                round2(periodPayment),
                round2(paid),
                round2(outstanding),
                round2(outstanding));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> invoices(Long studentId) {
        return invoiceRepository.findByStudentIdOrderByIssueDateDesc(studentId).stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstallmentResponse> installments(Long studentId) {
        return installmentRepository.findByStudentIdOrderByInstallmentNumberAsc(studentId).stream()
                .map(this::toInstallmentResponse)
                .toList();
    }

    @Transactional
    public InvoiceResponse markInvoicePaid(Long studentId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (!invoice.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This invoice does not belong to you");
        }
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        return toInvoiceResponse(invoice);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private InvoiceResponse toInvoiceResponse(Invoice i) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        return new InvoiceResponse(
                i.getId(),
                i.getInvoiceNumber(),
                i.getDescription(),
                i.getIssueDate() != null ? i.getIssueDate().format(fmt) : null,
                i.getDueDate() != null ? i.getDueDate().format(fmt) : null,
                i.getAmount(),
                i.getStatus().name());
    }

    private InstallmentResponse toInstallmentResponse(Installment i) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        return new InstallmentResponse(
                i.getId(),
                i.getTerm(),
                i.getInstallmentNumber(),
                i.getDueDate() != null ? i.getDueDate().format(fmt) : null,
                i.getAmount(),
                i.getStatus().name());
    }
}
