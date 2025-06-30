package com.shuaibu.service.impl;

import com.shuaibu.dto.LowStockDto;
import com.shuaibu.dto.ProductDto;
import com.shuaibu.mapper.ProductMapper;
import com.shuaibu.model.LowStockModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.ProductService;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        return productRepository.findAll().stream().map(ProductMapper::mapToDto).toList();
    }

    @Override
    public ProductDto getProductById(Long id) {
        return mapToDto(productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!")));
    }

    @Override
    public void saveOrUpdateProduct(ProductDto productDto) {
        boolean isNew = (productDto.getId() == null);

        if (isNew) {
            ProductModel existingProduct = productRepository.findByName(productDto.getName());
            if (existingProduct != null) {
                throw new IllegalArgumentException("Product with this barcode already exists");
            }
        } else {
            boolean isDuplicate = productRepository.existsByNameAndIdNot(productDto.getName(), productDto.getId());
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

    @Override
    public void handlePurchaseAndUpdateProduct(Long id, LowStockDto dto) {
        // ✅ Fetch product from DB
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // ✅ Create LowStock entry from dto
        LowStockModel lowStock = LowStockModel.builder()
                .id(null)
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .expiryDate(dto.getExpiryDate())
                .lowStockDate(dto.getLowStockDate())
                .nafdac(dto.getNafdac())
                .createdAt(LocalDateTime.now()) // 🔥 manually set
                .updatedAt(LocalDateTime.now())
                .build();

        lowStockRepository.save(lowStock);

        // ✅ Update product with latest data
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setExpiryDate(dto.getExpiryDate());
        product.setLowStockDate(dto.getLowStockDate());
        product.setNafdac(dto.getNafdac());

        productRepository.save(product);
    }

}
