package com.shuaibu.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerStatementDto {

    private LocalDateTime transactionDate;
    private String narration;
    private double debit;
    private double credit;
    private double balance;
}