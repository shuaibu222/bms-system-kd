package com.shuaibu.mapper;

import com.shuaibu.dto.DepositDto;
import com.shuaibu.model.DepositModel;

public class DepositMapper {

    public static DepositDto mapToDto(DepositModel depositModel) {
        return DepositDto.builder()
                .id(depositModel.getId())
                .totalAmount(depositModel.getTotalAmount())
                .paymentMethod(depositModel.getPaymentMethod())
                .depositDate(depositModel.getDepositDate())
                .purpose(depositModel.getPurpose())
                .customerId(depositModel.getCustomerId())
                .build();
    }

    public static DepositModel mapToModel(DepositDto depositDto) {        

        return DepositModel.builder()
                .id(depositDto.getId())
                .totalAmount(depositDto.getTotalAmount())
                .paymentMethod(depositDto.getPaymentMethod())
                .depositDate(depositDto.getDepositDate())
                .purpose(depositDto.getPurpose())
                .customerId(depositDto.getCustomerId())
                .build();
    }
}
