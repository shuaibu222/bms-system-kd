package com.shuaibu.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositDto {

    private Long id;
    private Long customerId;

    @NotNull(message = "* Total amount is required")
    private Double totalAmount;

    @NotNull(message = "* Payment method is required")
    private String paymentMethod;

    private String purpose;

    @NotNull(message = "* Deposit date is required")
    private LocalDate depositDate;

}
