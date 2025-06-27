package com.shuaibu.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDto {
    private Long id;

    @NotEmpty(message = "* Product name is mandatory")
    private String name;

    private Double price;

    private Integer quantity;

    @NotNull(message = "* Low Quantity Alert is mandatory")
    private Integer lowQuantityAlert;

    private Integer soldQuantity;

    private LocalDate expiryDate;

    private String nafdac;

    private LocalDate lowStockDate;

}