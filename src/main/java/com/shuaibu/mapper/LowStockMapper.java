package com.shuaibu.mapper;

import com.shuaibu.dto.LowStockDto;
import com.shuaibu.model.LowStockModel;

public class LowStockMapper {
    
    public static LowStockDto mapToDto(LowStockModel lowStockModel) {
        return LowStockDto.builder()
                .id(lowStockModel.getId())
                .name(lowStockModel.getName())
                .price(lowStockModel.getPrice())
                .quantity(lowStockModel.getQuantity())
                .lowStockDate(lowStockModel.getLowStockDate())
                .expiryDate(lowStockModel.getExpiryDate())
                .nafdac(lowStockModel.getNafdac())
                .build();
    }

    public static LowStockModel mapToModel(LowStockDto lowStockDto) {
        return LowStockModel.builder()
                .id(lowStockDto.getId())
                .name(lowStockDto.getName())
                .price(lowStockDto.getPrice())
                .quantity(lowStockDto.getQuantity())
                .lowStockDate(lowStockDto.getLowStockDate())
                .expiryDate(lowStockDto.getExpiryDate())
                .nafdac(lowStockDto.getNafdac())
                .build();
    }
}
