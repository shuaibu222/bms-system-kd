package com.shuaibu.model;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MainLedgerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String particulars; // e.g., "Cash", "Transfer", "Card", "POS", or "PURCHASE"
    private double credit; // Total sales amount (if it's a sale)
    private double debit;  // Total purchase amount (if it's a purchase)
    private double closingBalance;
    private double openingBalance; // Optional, if you want to track opening balance per entry
}
