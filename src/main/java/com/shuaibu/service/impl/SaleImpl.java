package com.shuaibu.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.SaleService;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.SaleMapper.*;

@Service
public class SaleImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;

    public SaleImpl(SaleRepository saleRepository, JavaMailSender javaMailSender,
            InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
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
                        .phone(sale.getPhone())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public SaleDto getSaleById(Long id) {
        return mapToDto(saleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sale not found!")));
    }

    @Transactional
    public void saveOrUpdateSale(SaleDto saleDto) {
        String customerName = saleDto.getCustomerName();

        // Customer lookup by phone fallback
        if (customerName == null || customerName.trim().isEmpty()) {
            if (saleDto.getPhone() != null && !saleDto.getPhone().trim().isEmpty()) {
                Optional<CustomerModel> customerOpt = customerRepository.findByPhone(saleDto.getPhone());
                if (customerOpt.isPresent()) {
                    customerName = customerOpt.get().getName();
                } else {
                    throw new IllegalArgumentException("Customer not found for phone: " + saleDto.getPhone());
                }
            } else {
                throw new IllegalArgumentException("Both customer name and phone number are missing.");
            }
        }

        SaleModel sale = SaleModel.builder()
                .qtnNum(generateQuotationNumber())
                .customerName(customerName)
                .phone(saleDto.getPhone())
                .totalAmount(saleDto.getTotalAmount())
                .saleDateTime(saleDto.getSaleDateTime())
                .build();

        List<SaleItemModel> saleItems = new ArrayList<>();

        for (SaleItemDto item : saleDto.getItems()) {
            SaleItemModel saleItem = SaleItemModel.builder()
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .sale(sale)
                    .build();

            saleItems.add(saleItem);
        }

        if (saleItems.isEmpty()) {
            throw new IllegalArgumentException("No items added to quotation.");
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
                .phone(latestSale.getPhone())
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