package com.shuaibu.dto;

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

    @NotEmpty(message = "* Unit is mandatory")
    private String unit;

    @NotEmpty(message = "* Store is mandatory")
    private String store;

    @NotNull(message = "* Price is mandatory")
    private Double price;

    @NotNull(message = "* Cost is mandatory")
    private Double cost;

    @NotNull(message = "* Stock is mandatory")
    private Integer quantity;

    @NotNull(message = "* Low Quantity Alert is mandatory")
    private Integer lowQuantityAlert;

    @NotEmpty(message = "* Category is mandatory")
    private String category;

    private Integer soldQuantity;
}