package com.shuaibu.mapper;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;

public class SaleMapper {

    public static SaleDto mapToDto(SaleModel saleModel) {
        return SaleDto.builder()
                .id(saleModel.getId())
                .qtnNum(saleModel.getQtnNum())
                .customerName(saleModel.getCustomerName())
                .totalAmount(saleModel.getTotalAmount())
                .phone(saleModel.getPhone())
                .saleDateTime(saleModel.getSaleDateTime())
                .build();
    }

    public static SaleModel mapToModel(SaleDto saleDto) {
        return SaleModel.builder()
                .id(saleDto.getId())
                .qtnNum(saleDto.getQtnNum())
                .customerName(saleDto.getCustomerName())
                .totalAmount(saleDto.getTotalAmount())
                .phone(saleDto.getPhone())
                .saleDateTime(saleDto.getSaleDateTime())
                .build();
    }

    public static SaleItemDto mapItemToDto(SaleItemModel saleItemModel) {
        return SaleItemDto.builder()
                .id(saleItemModel.getId())
                .productName(saleItemModel.getProductName())
                .quantity(saleItemModel.getQuantity())
                .price(saleItemModel.getPrice())
                .returnReason(saleItemModel.getReturnReason())
                .returnedQuantity(saleItemModel.getReturnedQuantity())
                .build();
    }
}