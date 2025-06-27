package com.shuaibu.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffDto {

    private Long id;

    @NotEmpty(message = "* Name is required")
    private String name;

    private String phone;

    private String address;

}