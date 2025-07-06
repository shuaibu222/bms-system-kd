package com.shuaibu.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseReturnDto {

    private Long id;

    @NotEmpty(message = "* Supplier name is required")
    private String supplierName;

    @NotEmpty(message = "* Product name is required")
    private String productName;

    @NotNull(message = "* Quantity is required")
    private Integer quantity;

    @NotEmpty(message = "* Store name is required")
    private String storeName;

    @NotNull(message = "* Total amount is required")
    private Double totalAmount;

    @NotEmpty(message = "* Reason is required")
    private String reason;

    @NotNull(message = "* Purchase return date and time is mandatory")
    private LocalDate date;

}