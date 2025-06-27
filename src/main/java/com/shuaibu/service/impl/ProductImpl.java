package com.shuaibu.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.ProductDto;
import com.shuaibu.mapper.ProductMapper;
import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.ProductService;

import java.util.List;

import static com.shuaibu.mapper.ProductMapper.*;

@Service
public class ProductImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductImpl(ProductRepository productRepository, JavaMailSender javaMailSender,
            SaleRepository saleRepository, InvoiceRepository invoiceRepository,
            ExpenseRepository expenseRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(ProductMapper::mapToDto).toList();
    }

    @Override
    public ProductDto getProductById(Long id) {
        return mapToDto(
                productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found!")));
    }

    @Override
    public void saveOrUpdateProduct(ProductDto productDto) {
        boolean isNew = (productDto.getId() == null);

        if (isNew) {
            // Check for duplicates for new entry
            ProductModel existingProduct = productRepository.findByName(productDto.getName());
            if (existingProduct != null) {
                throw new IllegalArgumentException("Product with this barcode already exists");
            }
        } else {
            // Check for duplicates for existing entry
            boolean isDuplicate = productRepository.existsByNameAndIdNot(
                    productDto.getName(),
                    productDto.getId());
            if (isDuplicate) {
                throw new IllegalArgumentException("Duplicate product found with the same barcode");
            }
        }

        productRepository.save(mapToModel(productDto));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public void saveAll(List<ProductDto> products) {
        // productRepository.saveAll(ProductMapper.mapToModel(products));
    }

}