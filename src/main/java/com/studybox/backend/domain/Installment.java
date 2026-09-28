package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** One scheduled payment within a student's tuition payment arrangement. */
@Entity
@Table(name = "installments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    private String term;

    private int installmentNumber;

    private LocalDate dueDate;

    private double amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InstallmentStatus status = InstallmentStatus.UNPAID;
}
