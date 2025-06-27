package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SaleDto {
    private Long id;

    private String qtnNum;
    private String customerName;
    private Integer phone;

    @NotNull(message = "* Total amount is mandatory")
    private Double totalAmount;

    @NotNull(message = "* Sales date is mandatory")
    private LocalDate saleDateTime;

    private String salesAgent;

    @NotEmpty(message = "* Sale items are mandatory")
    private List<SaleItemDto> items;

}
