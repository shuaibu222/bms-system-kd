package com.shuaibu.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.ProductDto;
import com.shuaibu.mapper.ProductMapper;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.PurchaseRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.ProductService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.shuaibu.mapper.ProductMapper.*;

@Service
public class ProductImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final InvoiceRepository invoiceRepository;

    public ProductImpl(ProductRepository productRepository, JavaMailSender javaMailSender,
            SaleRepository saleRepository, InvoiceRepository invoiceRepository,
            ExpenseRepository expenseRepository, PurchaseRepository purchaseRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.invoiceRepository = invoiceRepository;
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

    public List<ProductDto> getProductReport() {
        // Get today's date
        LocalDate today = LocalDate.now();

        // Fetch all products
        List<ProductModel> products = productRepository.findAll();

        // Fetch all invoices and filter by today's date
        List<InvoiceModel> invoices = invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getInvoiceDateTime().isEqual(today))
                .collect(Collectors.toList());

        return products.stream().map(product -> {
            // Calculate sold quantity by filtering items from linked quotations in invoices
            int soldQuantity = invoices.stream()
                    .flatMap(invoice -> {
                        // Fetch the linked quotation (sale) for each invoice
                        SaleModel quotation = saleRepository.findById(invoice.getQuotationId()).orElse(null);
                        return quotation != null ? quotation.getItems().stream() : Stream.empty();
                    })
                    .filter(item -> item.getProductName().equals(product.getName()))
                    .mapToInt(SaleItemModel::getQuantity)
                    .sum();

            // Build ProductDTO using Builder pattern
            return ProductDto.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .store(product.getStore())
                    .quantity(product.getQuantity())
                    .price(product.getPrice())
                    .cost(product.getCost())
                    .category(product.getCategory())
                    .unit(product.getUnit())
                    .lowQuantityAlert(product.getLowQuantityAlert())
                    .soldQuantity(soldQuantity)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<ProductDto> products) {
        // productRepository.saveAll(ProductMapper.mapToModel(products));
    }

}