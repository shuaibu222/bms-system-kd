package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDto {

    private Long id;

    @NotEmpty(message = "* Description is required")
    private String description;

    @NotNull(message = "* Amount is required")
    private Double amount;

    private LocalDate date;
}