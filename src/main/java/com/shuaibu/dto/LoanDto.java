package com.shuaibu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LoanDto {
    private Long id;
    
    @NotNull(message = "* Staff ID is required")
    private Long staffId;

    @NotNull(message = "* Loan amount is required")
    private Double amount;

    @NotNull(message = "* Purpose is required")
    private String purpose;

    @NotNull(message = "* Loan date is required")
    private LocalDate loanDate;

    private Double amountRepaid;
}
