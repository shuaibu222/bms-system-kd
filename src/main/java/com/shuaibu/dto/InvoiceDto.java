package com.shuaibu.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

    private Long id;
    private String invNum;
    private Long customerId;
    private String customerName;
    private String phone;
    private Long quotationId;
    private Double totalAmount;
    private Double invoiceValue;
    private String paymentMethod;

    @NotNull(message = "* Invoice date and time is required")
    private LocalDate invoiceDateTime;

    private String cashierAgent;

    private SaleDto saleDto;
    private String returnNotes;

}
