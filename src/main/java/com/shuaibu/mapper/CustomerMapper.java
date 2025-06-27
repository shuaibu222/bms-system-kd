package com.shuaibu.mapper;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.model.CustomerModel;

public class CustomerMapper {

    public static CustomerDto mapToDto(CustomerModel customerModel) {
        return CustomerDto.builder()
                .id(customerModel.getId())
                .name(customerModel.getName())
                .phone(customerModel.getPhone())
                .address(customerModel.getAddress())
                .balance(customerModel.getBalance())
                .build();
    }

    public static CustomerModel mapToModel(CustomerDto customerDto) {
        return CustomerModel.builder()
                .id(customerDto.getId())
                .name(customerDto.getName())
                .phone(customerDto.getPhone())
                .address(customerDto.getAddress())
                .balance(customerDto.getBalance())
                .build();
    }
}