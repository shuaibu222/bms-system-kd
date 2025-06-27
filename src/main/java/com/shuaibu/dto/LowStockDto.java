package com.shuaibu.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowStockDto {
    private Long id;

    @NotEmpty(message = "* Product name is mandatory")
    private String name;
    
    @NotNull(message = "* Price is mandatory")
    private Double price;

    @NotNull(message = "* Stock is mandatory")
    private Integer quantity;

    private LocalDate lowStockDate;

    private LocalDate expiryDate;

    private String nafdac;

}