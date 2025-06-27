package com.shuaibu.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private Double cashPaid;
    private Double cardPaid;
    private Double otherPaid;
    private Double totalPaid;
    private Double balanceDue;

    @NotNull(message = "* Payment status is required")
    private String paymentStatus;

    @NotNull(message = "* Payment method is required")
    private String paymentMethod;

    @NotNull(message = "* Invoice date and time is required")
    private LocalDate invoiceDateTime;

    private String cashierAgent;

    private SaleDto saleDto;
}
