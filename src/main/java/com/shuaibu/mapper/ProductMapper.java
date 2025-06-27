package com.shuaibu.mapper;

import com.shuaibu.dto.ProductDto;
import com.shuaibu.model.ProductModel;

public class ProductMapper {

    public static ProductDto mapToDto(ProductModel productModel) {
        return ProductDto.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .price(productModel.getPrice())
                .quantity(productModel.getQuantity())
                .lowQuantityAlert(productModel.getLowQuantityAlert())
                .nafdac(productModel.getNafdac())
                .expiryDate(productModel.getExpiryDate())
                .lowStockDate(productModel.getLowStockDate())
                .build();
    }

    public static ProductModel mapToModel(ProductDto productDto) {
        return ProductModel.builder()
                .id(productDto.getId())
                .name(productDto.getName())
                .price(productDto.getPrice())
                .quantity(productDto.getQuantity())
                .lowQuantityAlert(productDto.getLowQuantityAlert())
                .nafdac(productDto.getNafdac())
                .expiryDate(productDto.getExpiryDate())
                .lowStockDate(productDto.getLowStockDate())
                .build();
    }
}
