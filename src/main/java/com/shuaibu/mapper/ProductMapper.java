package com.shuaibu.mapper;

import com.shuaibu.dto.ProductDto;
import com.shuaibu.model.ProductModel;

public class ProductMapper {
    
    public static ProductDto mapToDto(ProductModel productModel) {
        return ProductDto.builder()
                .id(productModel.getId())
                .name(productModel.getName())
                .unit(productModel.getUnit())
                .store(productModel.getStore())
                .price(productModel.getPrice())
                .cost(productModel.getCost())
                .quantity(productModel.getQuantity())
                .lowQuantityAlert(productModel.getLowQuantityAlert())
                .category(productModel.getCategory())
                .build();
    }

    public static ProductModel mapToModel(ProductDto productDto) {
        return ProductModel.builder()
                .id(productDto.getId())
                .name(productDto.getName())
                .unit(productDto.getUnit())
                .store(productDto.getStore())
                .price(productDto.getPrice())
                .cost(productDto.getCost())
                .quantity(productDto.getQuantity())
                .lowQuantityAlert(productDto.getLowQuantityAlert())
                .category(productDto.getCategory())
                .build();
    }
}
