package com.shuaibu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesReturnDto {

    private Long id;

    @NotEmpty(message = "* Customer name is required")
    private String customerName;

    @NotNull(message = "* Total amount is required")
    private Double totalAmount;

    @NotEmpty(message = "* Reason is required")
    private String reason;

    @NotNull(message = "* Sale return date and time is mandatory")
    private LocalDateTime saleRDateTime;
}