package com.shuaibu.service;

import java.util.List;

import com.shuaibu.dto.ProductDto;

public interface ProductService {
    List<ProductDto> getAllProducts();

    ProductDto getProductById(Long id);

    void saveOrUpdateProduct(ProductDto productDto);

    void deleteProduct(Long id);

    void saveAll(List<ProductDto> products);
}