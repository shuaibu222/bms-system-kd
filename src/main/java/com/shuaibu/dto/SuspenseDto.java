package com.shuaibu.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SuspenseDto {

    private Long id;

    private String reason;
    private Double amount;
    private String paymentMethod;
    private LocalDate transactionDate;
    private String referenceId;

    private String status;
}