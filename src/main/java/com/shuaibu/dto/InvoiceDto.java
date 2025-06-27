package com.shuaibu.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceDto {

    private Long id;
    private String invNum;
    private Long quotationId;

    @NotNull(message = "* Total amount is required")
    private Double totalAmount;

    private Double invoiceValue;
    private Double balanceDue;

    private String paymentStatus;

    private String paymentMethod;

    @NotNull(message = "* Invoice date and time is required")
    private LocalDate invoiceDateTime;

    private String cashierAgent;

    private SaleDto saleDto;

}
