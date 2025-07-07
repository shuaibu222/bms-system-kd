package com.shuaibu.service.impl;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.SaleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.SaleMapper.*;

@Service
@RequiredArgsConstructor
public class SaleImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<SaleModel> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateTimeBetween(startDate, endDate);
    }

    @Override
    public List<SaleDto> getAllSales() {
        return saleRepository.findAll()
                .stream()
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
        return mapToDto(saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with ID: " + id)));
    }

    @Override
    @Transactional
    public void saveOrUpdateSale(SaleDto saleDto) {
        if (saleDto == null) {
            throw new IllegalArgumentException("Sale data cannot be null");
        }

        String customerName = saleDto.getCustomerName();

        // 📞 Fallback: use phone number to find customer if name is empty
        if (customerName == null || customerName.trim().isEmpty()) {
            if (saleDto.getPhone() != null && !saleDto.getPhone().trim().isEmpty()) {
                Optional<CustomerModel> customerOpt = customerRepository.findByPhone(saleDto.getPhone());
                if (customerOpt.isPresent()) {
                    customerName = customerOpt.get().getName();
                } else {
                    throw new IllegalArgumentException("No customer found with phone: " + saleDto.getPhone());
                }
            } else {
                throw new IllegalArgumentException("Customer name or phone must be provided");
            }
        }

        if (saleDto.getItems() == null || saleDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quotation must contain at least one item");
        }

        // 🧾 Create the SaleModel
        SaleModel sale = SaleModel.builder()
                .qtnNum(generateQuotationNumber())
                .customerName(customerName)
                .phone(saleDto.getPhone())
                .totalAmount(saleDto.getTotalAmount())
                .saleDateTime(saleDto.getSaleDateTime())
                .build();

        // 🧾 Convert each SaleItemDto into SaleItemModel
        List<SaleItemModel> saleItems = saleDto.getItems().stream()
                .map(item -> SaleItemModel.builder()
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .sale(sale) // Link item to parent sale
                        .build())
                .collect(Collectors.toList());

        sale.setItems(saleItems);
        saleRepository.save(sale);
    }

    @Override
    public void deleteSale(Long id) {
        if (!saleRepository.existsById(id)) {
            throw new IllegalArgumentException("Sale not found with ID: " + id);
        }
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
                .phone(latestSale.getPhone())
                .totalAmount(latestSale.getTotalAmount())
                .saleDateTime(latestSale.getSaleDateTime())
                .items(latestSale.getItems().stream().map(item -> SaleItemDto.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .build();
    }

    private String generateQuotationNumber() {
        return "QTN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
