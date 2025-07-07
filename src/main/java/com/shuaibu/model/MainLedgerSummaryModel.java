package com.shuaibu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MainLedgerSummaryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate summaryDate; // e.g., June 30, 2025

    private double totalExpenses;
    private double customerPayments;
    private double profitOrLoss;
    private double finalLedgerBalance;
    private double stockValue;
    private double customerDebt;
    private double suspense;
    private double staffLoans;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
