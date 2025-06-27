package com.shuaibu.service.impl;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleDto;
import com.shuaibu.dto.SaleItemDto;
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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final JavaMailSender javaMailSender;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public InvoiceImpl(InvoiceRepository invoiceRepository, JavaMailSender javaMailSender,
            SaleRepository saleRepository, ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
        this.javaMailSender = javaMailSender;
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

        // Compute total paid and balance due
        double totalPaid = (invoiceDto.getCardPaid() != null ? invoiceDto.getCardPaid() : 0) +
                (invoiceDto.getCashPaid() != null ? invoiceDto.getCashPaid() : 0) +
                (invoiceDto.getOtherPaid() != null ? invoiceDto.getOtherPaid() : 0);
        double balanceDue = invoiceDto.getTotalAmount() - totalPaid;

        // Set total paid and balance due
        invoiceDto.setTotalPaid(totalPaid);
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
                .cashPaid(latestInvoice.getCashPaid())
                .cardPaid(latestInvoice.getCardPaid())
                .otherPaid(latestInvoice.getOtherPaid())
                .totalPaid(latestInvoice.getTotalPaid())
                .balanceDue(latestInvoice.getBalanceDue())
                .paymentStatus(latestInvoice.getPaymentStatus())
                .paymentMethod(latestInvoice.getPaymentMethod())
                .invoiceDateTime(latestInvoice.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

    public void sendInvoiceEmail(String email) {
        InvoiceModel latestInvoice = invoiceRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("No invoices found"));

        String subject = "Your Invoice from My Store";
        String body = generateEmailContent(latestInvoice);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Failed to send email", e);
        }
    }

    private String generateEmailContent(InvoiceModel invoice) {
        SaleModel sale = saleRepository.findById(invoice.getQuotationId())
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        StringBuilder content = new StringBuilder();
        content.append("<h2>Invoice from My Store</h2>");
        content.append("<p><strong>Invoice No.:</strong> " + invoice.getInvNum() + "</p>");
        content.append("<p><strong>Customer:</strong> " + sale.getCustomerName() + "</p>");
        content.append("<p><strong>Total Amount:</strong> ₦" + invoice.getTotalAmount() + "</p>");
        content.append("<p><strong>Cash Paid:</strong> ₦" + (invoice.getCashPaid() != null ? invoice.getCashPaid() : 0)
                + "</p>");
        content.append("<p><strong>Card Paid:</strong> ₦" + (invoice.getCardPaid() != null ? invoice.getCardPaid() : 0)
                + "</p>");
        content.append("<p><strong>Other Paid:</strong> ₦"
                + (invoice.getOtherPaid() != null ? invoice.getOtherPaid() : 0) + "</p>");
        content.append("<p><strong>Total Paid:</strong> ₦" + invoice.getTotalPaid() + "</p>");
        content.append("<p><strong>Balance Due:</strong> ₦" + invoice.getBalanceDue() + "</p>");
        content.append("<p><strong>Payment Status:</strong> " + invoice.getPaymentStatus() + "</p>");
        content.append("<p><strong>Date:</strong> " + invoice.getInvoiceDateTime() + "</p>");

        content.append("<h3>Items Purchased</h3>");
        content.append(
                "<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        content.append("<tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr>");

        sale.getItems().forEach(item -> content.append(
                "<tr>" +
                        "<td>" + item.getProductName() + "</td>" +
                        "<td>" + item.getQuantity() + "</td>" +
                        "<td>₦" + item.getPrice() + "</td>" +
                        "<td>₦" + (item.getQuantity() * item.getPrice()) + "</td>" +
                        "</tr>"));

        content.append("</table>");
        content.append("<h3>Total: ₦" + sale.getTotalAmount() + "</h3>");
        content.append("<p>Thank you for shopping with us!</p>");

        return content.toString();
    }
}
