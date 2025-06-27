package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseDto {

    private Long id;

    @NotEmpty(message = "* Description is required")
    private String description;

    @NotNull(message = "* Amount is required")
    private BigDecimal amount;

    private LocalDate date;
}