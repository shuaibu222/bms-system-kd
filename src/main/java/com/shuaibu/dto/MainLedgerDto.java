package com.shuaibu.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainLedgerDto {
    private Long id;
    private LocalDate date;
    private String particulars; // e.g., "Cash", "Transfer", "Card", "POS", or "PURCHASE"
    private double credit; // Total sales amount (if it's a sale)
    private double debit; // Total purchase amount (if it's a purchase)
    private double closingBalance;
    private double openingBalance;

}
