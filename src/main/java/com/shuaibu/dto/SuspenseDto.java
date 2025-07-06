package com.shuaibu.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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