package com.shuaibu.mapper;

import com.shuaibu.dto.SalesReturnDto;
import com.shuaibu.model.SalesReturnModel;

public class SalesReturnMapper {

    public static SalesReturnDto mapToDto(SalesReturnModel salesReturnModel) {
        return SalesReturnDto.builder()
                .id(salesReturnModel.getId())
                .customerName(salesReturnModel.getCustomerName())
                .totalAmount(salesReturnModel.getTotalAmount())
                .reason(salesReturnModel.getReason())
                .saleRDateTime(salesReturnModel.getSaleRDateTime())
                .build();
    }

    public static SalesReturnModel mapToModel(SalesReturnDto salesReturnDto) {
        return SalesReturnModel.builder()
                .id(salesReturnDto.getId())
                .customerName(salesReturnDto.getCustomerName())
                .totalAmount(salesReturnDto.getTotalAmount())
                .reason(salesReturnDto.getReason())
                .saleRDateTime(salesReturnDto.getSaleRDateTime())
                .build();
    }
}