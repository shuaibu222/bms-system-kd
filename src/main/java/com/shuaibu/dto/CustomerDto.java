package com.shuaibu.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDto {

    private Long id;

    private String name;

    private String phone;

    private String address;

    private Double balance;
}