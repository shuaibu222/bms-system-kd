package com.shuaibu.service.impl;

import com.shuaibu.dto.*;
import com.shuaibu.mapper.*;
import com.shuaibu.model.*;
import com.shuaibu.repository.*;
import com.shuaibu.service.InvoiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerStatementRepository customerStatementRepository;

    @Override
    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        return InvoiceMapper.mapToDto(
                invoiceRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invoice not found!")));
    }

    @Override
    public InvoiceModel saveOrUpdateInvoice(InvoiceDto invoiceDto) {
        if (invoiceDto == null) {
            throw new IllegalArgumentException("Invoice DTO cannot be null");
        }
        if (invoiceDto.getQuotationId() == null) {
            throw new IllegalArgumentException("Quotation ID must be provided");
        }

        // Generate invoice number if it's a new invoice
        if (invoiceDto.getId() == null) {
            invoiceDto.setInvNum(generateInvoiceNumber());
        }

        // Fetch quotation
        SaleModel quotation = saleRepository.findById(invoiceDto.getQuotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found with ID: " + invoiceDto.getQuotationId()));

        // Optional: Fetch customer by phone number
        if (quotation.getPhone() != null && !quotation.getPhone().trim().isEmpty()) {
            customerRepository.findByPhone(quotation.getPhone()).ifPresentOrElse(customer -> {
                invoiceDto.setCustomerId(customer.getId());
                invoiceDto.setCustomerName(customer.getName());
                invoiceDto.setPhone(customer.getPhone());

                double currentBalance = Optional.ofNullable(customer.getBalance()).orElse(0.0);
                double invoiceValue = invoiceDto.getInvoiceValue();

                // Update balance
                customer.setBalance(currentBalance - invoiceValue);
                customerRepository.save(customer);

                // Create statement record
                CustomerStatementModel statement = new CustomerStatementModel();
                statement.setCustomerId(customer.getId());
                statement.setTransactionDate(LocalDateTime.now());
                statement.setNarration("Sales (" + invoiceDto.getInvNum() + ")");
                statement.setDebit(invoiceValue);
                statement.setCredit(0.0);
                statement.setBalance(currentBalance - invoiceValue); // new balance after sale

                customerStatementRepository.save(statement);

            }, () -> {
                throw new IllegalArgumentException("Customer with phone " + quotation.getPhone() + " not found");
            });
        } else {
            invoiceDto.setCustomerName(quotation.getCustomerName());
        }

        invoiceDto.setTotalAmount(quotation.getTotalAmount());

        // Deduct stock
        for (SaleItemModel item : quotation.getItems()) {
            ProductModel product = productRepository.findByName(item.getProductName());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + item.getProductName());
            }
            if (product.getQuantity() == null || product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + item.getProductName());
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        return invoiceRepository.save(InvoiceMapper.mapToModel(invoiceDto));
    }

    public String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    @Override
    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    @Override
    public InvoiceDto getLatestInvoice() {
        InvoiceModel latestInvoice = invoiceRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("No invoices found"));

        if (latestInvoice.getQuotationId() == null) {
            throw new IllegalArgumentException("Quotation ID is missing for the latest invoice");
        }

        SaleModel sale = saleRepository.findById(latestInvoice.getQuotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sale not found for Quotation ID: " + latestInvoice.getQuotationId()));

        SaleDto saleDto = SaleDto.builder()
                .id(sale.getId())
                .qtnNum(sale.getQtnNum())
                .customerName(sale.getCustomerName())
                .phone(sale.getPhone())
                .totalAmount(sale.getTotalAmount())
                .saleDateTime(sale.getSaleDateTime())
                .items(sale.getItems().stream()
                        .map(SaleMapper::mapItemToDto)
                        .collect(Collectors.toList()))
                .build();

        return InvoiceDto.builder()
                .id(latestInvoice.getId())
                .invNum(latestInvoice.getInvNum())
                .quotationId(latestInvoice.getQuotationId())
                .customerId(latestInvoice.getCustomerId())
                .customerName(latestInvoice.getCustomerName())
                .phone(latestInvoice.getPhone())
                .totalAmount(latestInvoice.getTotalAmount())
                .invoiceValue(latestInvoice.getInvoiceValue())
                .paymentMethod(latestInvoice.getPaymentMethod())
                .invoiceDateTime(latestInvoice.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

    @Override
    public InvoiceDto getInvoiceWithItems(Long invoiceId) {
        InvoiceModel inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        SaleDto saleDto = null;
        if (inv.getQuotationId() != null) {
            Long qtnId = inv.getQuotationId();

            SaleModel sale = saleRepository.findById(qtnId).orElse(null);

            if (sale != null) {
                saleDto = SaleDto.builder()
                        .id(sale.getId())
                        .qtnNum(sale.getQtnNum())
                        .customerName(sale.getCustomerName())
                        .phone(sale.getPhone())
                        .totalAmount(sale.getTotalAmount())
                        .saleDateTime(sale.getSaleDateTime())
                        .items(sale.getItems().stream()
                                .map(SaleMapper::mapItemToDto)
                                .collect(Collectors.toList()))
                        .build();
            }
        }

        return InvoiceDto.builder()
                .id(inv.getId())
                .invNum(inv.getInvNum())
                .quotationId(inv.getQuotationId())
                .customerId(inv.getCustomerId())
                .customerName(inv.getCustomerName())
                .phone(inv.getPhone())
                .totalAmount(inv.getTotalAmount())
                .invoiceValue(inv.getInvoiceValue())
                .paymentMethod(inv.getPaymentMethod())
                .invoiceDateTime(inv.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

}
