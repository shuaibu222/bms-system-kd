package com.shuaibu.service.impl;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleDto;
import com.shuaibu.mapper.InvoiceMapper;
import com.shuaibu.mapper.SaleMapper;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.InvoiceService;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public InvoiceImpl(InvoiceRepository invoiceRepository, SaleRepository saleRepository,
            ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
        this.saleRepository = saleRepository;
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
    @Transactional
    public InvoiceModel saveOrUpdateInvoice(InvoiceDto invoiceDto) {
        // Generate invoice number
        invoiceDto.setInvNum(generateInvoiceNumber());

        double balanceDue = invoiceDto.getTotalAmount() - invoiceDto.getInvoiceValue();

        invoiceDto.setBalanceDue(balanceDue);

        // Fetch the associated quotation (or sale) using quotationId
        if (invoiceDto.getQuotationId() != null) {
            SaleModel quotation = saleRepository.findById(invoiceDto.getQuotationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Quotation not found with ID: " + invoiceDto.getQuotationId()));

            // Deduct stock for each item in the quotation
            for (SaleItemModel item : quotation.getItems()) {
                ProductModel product = productRepository.findByName(item.getProductName());

                if (product == null) {
                    throw new IllegalArgumentException("Product not found in store: ");
                }

                // Check stock
                if (product.getQuantity() < item.getQuantity()) {
                    // throw new IllegalArgumentException("Insufficient stock for product: " +
                    // product.getName());
                    System.out.println("Insufficient stock for samsung!");
                }

                // Deduct stock
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        // Save the invoice
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
                .totalAmount(latestInvoice.getTotalAmount())
                .balanceDue(latestInvoice.getBalanceDue())
                .paymentStatus(latestInvoice.getPaymentStatus())
                .paymentMethod(latestInvoice.getPaymentMethod())
                .invoiceDateTime(latestInvoice.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }
}
