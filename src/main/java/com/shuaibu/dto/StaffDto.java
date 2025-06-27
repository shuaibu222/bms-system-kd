package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffDto {

    private Long id;

    @NotEmpty(message = "* Name is required")
    private String name;

    @NotEmpty(message = "* Phone is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "* Phone must be 10 digits")
    private String phone;

    private String address;

}