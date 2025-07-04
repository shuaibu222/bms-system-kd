package com.shuaibu.service.impl;

import com.shuaibu.dto.*;
import com.shuaibu.mapper.*;
import com.shuaibu.model.*;
import com.shuaibu.repository.*;
import com.shuaibu.service.InvoiceService;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public InvoiceImpl(InvoiceRepository invoiceRepository, SaleRepository saleRepository,
            ProductRepository productRepository, CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
    }

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

        // Generate invoice number
        invoiceDto.setInvNum(generateInvoiceNumber());

        // Fetch quotation
        SaleModel quotation = saleRepository.findById(invoiceDto.getQuotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found with ID: " + invoiceDto.getQuotationId()));

        // Optional: Fetch customer if phone exists
        if (quotation.getPhone() != null && !quotation.getPhone().trim().isEmpty()) {
            customerRepository.findByPhone(quotation.getPhone()).ifPresent(customer -> {
                invoiceDto.setCustomerId(customer.getId());

                // Update customer balance
                double currentBalance = customer.getBalance() != null ? customer.getBalance() : 0.0;
                customer.setBalance(currentBalance - invoiceDto.getInvoiceValue());
                customerRepository.save(customer);
            });
        }

        // Set total and balance due
        invoiceDto.setTotalAmount(quotation.getTotalAmount());
        double balanceDue = quotation.getTotalAmount() - invoiceDto.getInvoiceValue();
        invoiceDto.setBalanceDue(balanceDue);

        // Deduct stock
        for (SaleItemModel item : quotation.getItems()) {
            ProductModel product = productRepository.findByName(item.getProductName());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + item.getProductName());
            }
            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + item.getProductName());
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Save and return invoice
        return invoiceRepository.save(InvoiceMapper.mapToModel(invoiceDto));
    }

    public String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    @Override
    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

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
                .balanceDue(latestInvoice.getBalanceDue())
                .totalAmount(latestInvoice.getTotalAmount())
                .invoiceValue(latestInvoice.getInvoiceValue())
                .paymentStatus(latestInvoice.getPaymentStatus())
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
            SaleModel sale = saleRepository.findById(inv.getQuotationId())
                    .orElse(null);

            if (sale != null) {
                saleDto = SaleDto.builder()
                        .id(sale.getId())
                        .qtnNum(sale.getQtnNum())
                        .customerName(sale.getCustomerName())
                        .phone(sale.getPhone())
                        .totalAmount(sale.getTotalAmount())
                        .saleDateTime(sale.getSaleDateTime())
                        .items(sale.getItems().stream()
                                .map(SaleMapper::mapItemToDto) // 🔁 Don't overwrite returnedQuantity
                                .collect(Collectors.toList()))
                        .build();
            }
        }

        return InvoiceDto.builder()
                .id(inv.getId())
                .invNum(inv.getInvNum())
                .quotationId(inv.getQuotationId())
                .customerId(inv.getCustomerId())
                .balanceDue(inv.getBalanceDue())
                .totalAmount(inv.getTotalAmount())
                .invoiceValue(inv.getInvoiceValue())
                .paymentStatus(inv.getPaymentStatus())
                .paymentMethod(inv.getPaymentMethod())
                .invoiceDateTime(inv.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

}
