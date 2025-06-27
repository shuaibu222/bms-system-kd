package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDto {

    private Long id;

    @NotEmpty(message = "* Name is required")
    private String name;

    @NotEmpty(message = "* Phone is required")
    private String phone;

    private String address;

    private Double balance;

}