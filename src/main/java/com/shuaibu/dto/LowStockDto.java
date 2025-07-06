package com.shuaibu.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LowStockDto {
    private Long id;

    private String name;

    @NotNull(message = "* Price is mandatory")
    private Double price;

    @NotNull(message = "* Stock is mandatory")
    private Integer quantity;

    private LocalDate lowStockDate;

    private LocalDate expiryDate;

    private String nafdac;

}