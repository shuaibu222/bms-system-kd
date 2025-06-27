package com.shuaibu.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.SaleService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.SaleMapper.*;

@Service
public class SaleImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final JavaMailSender javaMailSender;
    private final InvoiceRepository invoiceRepository;

    public SaleImpl(SaleRepository saleRepository, ProductRepository productRepository,
            JavaMailSender javaMailSender, InvoiceRepository invoiceRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.javaMailSender = javaMailSender;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<SaleModel> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateTimeBetween(startDate, endDate);
    }

    @Override
    public List<SaleDto> getAllSales() {
        List<SaleModel> sales = saleRepository.findAll();
        return sales.stream()
                .map(sale -> SaleDto.builder()
                        .id(sale.getId())
                        .customerName(sale.getCustomerName())
                        .totalAmount(sale.getTotalAmount())
                        .paymentMethod(sale.getPaymentMethod())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public SaleDto getSaleById(Long id) {
        return mapToDto(saleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sale not found!")));
    }

    @Transactional
    public void saveOrUpdateSale(SaleDto saleDto) {
        SaleModel sale = SaleModel.builder()
                .qtnNum(generateQuotationNumber())
                .customerName(saleDto.getCustomerName())
                .totalAmount(saleDto.getTotalAmount())
                .paymentMethod(saleDto.getPaymentMethod())
                .saleDateTime(saleDto.getSaleDateTime())
                .build();

        List<SaleItemModel> saleItems = new ArrayList<>();

        for (SaleItemDto item : saleDto.getItems()) {
            // Get the product from the database
            ProductModel product = productRepository.findByName(item.getProductName());

            if (product == null) {
                System.out.println("Product not found: " + item.getProductName());
                continue; // Skip this item
            }

            // Check if stock is sufficient
            if (product.getQuantity() < item.getQuantity()) {
                System.out.println("Skipping item due to insufficient stock: " + item.getProductName());
                continue; // Skip this item
            }

            // Create sale item entry
            SaleItemModel saleItem = SaleItemModel.builder()
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .sale(sale)
                    .build();

            saleItems.add(saleItem);
        }

        if (saleItems.isEmpty()) {
            throw new IllegalArgumentException("No items were added to the sale due to insufficient stock.");
        }

        sale.setItems(saleItems);
        saleRepository.save(sale);
    }

    public String generateQuotationNumber() {
        return "QTN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    @Override
    public SaleDto getLatestSale() {
        SaleModel latestSale = saleRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("No sales found"));

        return SaleDto.builder()
                .id(latestSale.getId())
                .qtnNum(latestSale.getQtnNum())
                .customerName(latestSale.getCustomerName())
                .totalAmount(latestSale.getTotalAmount())
                .paymentMethod(latestSale.getPaymentMethod())
                .saleDateTime(latestSale.getSaleDateTime())
                .items(latestSale.getItems().stream().map(item -> SaleItemDto.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .build();
    }
}