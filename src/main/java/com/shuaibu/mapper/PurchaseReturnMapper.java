package com.shuaibu.mapper;

import com.shuaibu.dto.PurchaseReturnDto;
import com.shuaibu.model.PurchaseReturnModel;

public class PurchaseReturnMapper {

    public static PurchaseReturnDto mapToDto(PurchaseReturnModel purchaseReturnModel) {
        return PurchaseReturnDto.builder()
                .id(purchaseReturnModel.getId())
                .supplierName(purchaseReturnModel.getSupplierName())
                .productName(purchaseReturnModel.getProductName())
                .quantity(purchaseReturnModel.getQuantity())
                .storeName(purchaseReturnModel.getStoreName())
                .totalAmount(purchaseReturnModel.getTotalAmount())
                .reason(purchaseReturnModel.getReason())
                .pReturnDateTime(purchaseReturnModel.getPReturnDateTime())
                .build();
    }

    public static PurchaseReturnModel mapToModel(PurchaseReturnDto purchaseReturnDto) {
        return PurchaseReturnModel.builder()
                .id(purchaseReturnDto.getId())
                .supplierName(purchaseReturnDto.getSupplierName())
                .productName(purchaseReturnDto.getProductName())
                .quantity(purchaseReturnDto.getQuantity())
                .storeName(purchaseReturnDto.getStoreName())
                .totalAmount(purchaseReturnDto.getTotalAmount())
                .reason(purchaseReturnDto.getReason())
                .pReturnDateTime(purchaseReturnDto.getPReturnDateTime())
                .build();
    }
}