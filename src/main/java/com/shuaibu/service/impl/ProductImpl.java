package com.shuaibu.service.impl;

import com.shuaibu.dto.LowStockDto;
import com.shuaibu.dto.ProductDto;
import com.shuaibu.mapper.ProductMapper;
import com.shuaibu.model.LowStockModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.*;

import com.shuaibu.service.ProductService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.ProductMapper.*;

@Service
public class ProductImpl implements ProductService {

    private final ProductRepository productRepository;
    private final LowStockRepository lowStockRepository;

    public ProductImpl(ProductRepository productRepository,
            LowStockRepository lowStockRepository,
            JavaMailSender javaMailSender,
            SaleRepository saleRepository,
            InvoiceRepository invoiceRepository,
            ExpenseRepository expenseRepository) {
        this.productRepository = productRepository;
        this.lowStockRepository = lowStockRepository;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        return mapToDto(productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id)));
    }

    @Override
    public void saveOrUpdateProduct(ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("Product data cannot be null");
        }

        boolean isNew = (productDto.getId() == null);

        if (isNew) {
            ProductModel existingProduct = productRepository.findByName(productDto.getName());
            if (existingProduct != null) {
                throw new IllegalArgumentException("Product with this name/barcode already exists");
            }

            productDto.setQuantity(0);
            productDto.setPrice(0.0);
        } else {
            boolean isDuplicate = productRepository.existsByNameAndIdNot(productDto.getName(), productDto.getId());
            if (isDuplicate) {
                throw new IllegalArgumentException("Another product exists with the same name/barcode");
            }
        }

        productRepository.save(mapToModel(productDto));
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public void saveAll(List<ProductDto> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Product list must not be null or empty");
        }

        List<ProductModel> productModels = products.stream()
                .map(ProductMapper::mapToModel)
                .collect(Collectors.toList());

        productRepository.saveAll(productModels);
    }

    @Override
    public void handlePurchaseAndUpdateProduct(Long id, LowStockDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("LowStock data cannot be null");
        }

        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        LowStockModel lowStock = LowStockModel.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .expiryDate(dto.getExpiryDate())
                .lowStockDate(dto.getLowStockDate() != null ? dto.getLowStockDate() : LocalDate.now())
                .nafdac(dto.getNafdac())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lowStockRepository.save(lowStock);

        // Update product with new stock
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setExpiryDate(dto.getExpiryDate());
        product.setLowStockDate(dto.getLowStockDate());
        product.setNafdac(dto.getNafdac());

        productRepository.save(product);
    }
}
