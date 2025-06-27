package com.shuaibu.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesReturnDto {

    private Long id;

    private String invNum;

    @NotEmpty(message = "* Reason is required")
    private String reason;

    @NotNull(message = "* Sale return date and time is mandatory")
    private LocalDate date;
}