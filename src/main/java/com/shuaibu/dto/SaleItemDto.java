package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleItemDto {
    private Long id;

    @NotEmpty(message = "* Product name is mandatory")
    private String productName;

    @NotNull(message = "* Quantity is mandatory")
    private Integer quantity;

    @NotNull(message = "* Price is mandatory")
    private Double price;

    private Integer returnedQuantity; // Track how many items were returned
    private String returnReason; // Optional: reason for return
}
