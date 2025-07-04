package com.shuaibu.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.model.SaleItemModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.InvoiceService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public String listInvoiceForm(Model model) {
        model.addAttribute("invoice", new InvoiceModel());
        return "invoices/list";
    }

    @GetMapping("/list")
    public String listInvoice(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAll());
        return "invoices/list-invoices";
    }

    @GetMapping("/search")
    public String getQuotationByQtnNum(@RequestParam String qtnNum, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel userModel = userRepository.findByUsername(authentication.getName());

        SaleModel quotation = saleRepository.findByQtnNum(qtnNum);

        if (quotation != null) {
            // Create invoice with default value
            InvoiceModel invoice = new InvoiceModel();
            invoice.setInvoiceValue(quotation.getTotalAmount());

            model.addAttribute("quotation", quotation);
            model.addAttribute("agent", userModel.getFullName());
            model.addAttribute("invoice", invoice); // Pass the prepared invoice
        }
        return "invoices/list"; // Return to the same view with the quotation and invoice form
    }

    @PostMapping
    public String createInvoice(@Valid @ModelAttribute("invoice") InvoiceDto invoice,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("invoice", invoice);
            return "invoices/list";
        }

        // Add debug logging
        System.out.println("Received invoice data: " + invoice);
        System.out.println("Quotation ID: " + invoice.getQuotationId());

        try {
            invoiceService.saveOrUpdateInvoice(invoice);
            return "redirect:/invoices/success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "invoices/list";
        }
    }

    @GetMapping("/edit/{id}")
    public String updateInvoiceForm(@PathVariable Long id, Model model) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        return "invoices/edit";
    }

    @PostMapping("/update/{id}")
    public String updateInvoice(@PathVariable Long id,
            @Valid @ModelAttribute("invoice") InvoiceDto invoice,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("invoice", invoice);
            return "invoices/edit";
        }
        invoice.setId(id);
        invoiceService.saveOrUpdateInvoice(invoice);
        return "redirect:/invoices?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return "redirect:/invoices/list?deleteSuccess";
    }

    @GetMapping("/success")
    public String invoiceSuccessPage() {
        return "invoices/invoice-success";
    }

    @GetMapping("/latest")
    @ResponseBody
    public ResponseEntity<?> getLatestInvoice() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel userModel = userRepository.findByUsername(authentication.getName());
        try {
            InvoiceDto invoiceDto = invoiceService.getLatestInvoice();
            invoiceDto.setCashierAgent(userModel.getFullName());

            return ResponseEntity.ok(invoiceDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage())); // Always return JSON
        }
    }

    @GetMapping("/sales-return/search")
    public String searchForSalesReturn(
            @RequestParam(required = false) String invNum,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        InvoiceModel invoice = null;

        // Case 1: Invoice number provided (highest priority)
        if (invNum != null && !invNum.isBlank()) {
            invoice = invoiceRepository.findByInvNum(invNum);
        }
        // Case 2: Date + Phone (customer account-based)
        else if (date != null && phone != null && !phone.isBlank()) {
            CustomerModel customer = customerRepository.findByPhone(phone).orElse(null);
            if (customer != null) {
                invoice = invoiceRepository.findFirstByCustomerIdAndInvoiceDateTime(customer.getId(), date);
            }
        }
        // Case 3: Only Date (walk-in customer)
        else if (date != null) {
            List<InvoiceModel> invoices = invoiceRepository.findByInvoiceDateTime(date);
            model.addAttribute("invoices", invoices);
            return "invoices/sales-return-list";
        }

        if (invoice != null) {
            InvoiceDto invoiceDto = invoiceService.getInvoiceWithItems(invoice.getId());

            // 🧹 Reset return values so the form starts clean
            if (invoiceDto.getSaleDto() != null && invoiceDto.getSaleDto().getItems() != null) {
                invoiceDto.getSaleDto().getItems().forEach(item -> {
                    item.setReturnedQuantity(null);
                    item.setReturnReason(null);
                });
            }

            model.addAttribute("invoice", invoiceDto);
            return "invoices/sales-return-process"; // Show the new return processing form
        } else {
            model.addAttribute("error", "No matching invoice found.");
            return "invoices/sales-return";
        }
    }

    @GetMapping("/sales-return/process/{id}")
    public String showSalesReturnForm(@PathVariable Long id, Model model) {
        InvoiceDto invoiceDto = invoiceService.getInvoiceWithItems(id);

        // 🧹 Reset return values so the form starts clean
        if (invoiceDto.getSaleDto() != null && invoiceDto.getSaleDto().getItems() != null) {
            invoiceDto.getSaleDto().getItems().forEach(item -> {
                item.setReturnedQuantity(null);
                item.setReturnReason(null);
            });
        }

        model.addAttribute("invoice", invoiceDto);
        return "invoices/sales-return-process"; // This view contains the return form
    }

    @PostMapping("/process-return/{id}")
    public String processReturn(
            @PathVariable Long id,
            @ModelAttribute("invoice") InvoiceDto invoiceDto,
            HttpSession session) {

        try {
            double totalRefund = invoiceDto.getSaleDto().getItems().stream()
                    .mapToDouble(item -> (item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0)
                            * item.getPrice())
                    .sum();

            InvoiceModel invoice = invoiceRepository.findById(id).orElseThrow();
            invoice.setTotalAmount(invoice.getTotalAmount() - totalRefund);

            SaleModel sale = saleRepository.findById(invoice.getQuotationId()).orElseThrow();

            List<SaleItemModel> itemsToRemove = new ArrayList<>();

            for (SaleItemDto dtoItem : invoiceDto.getSaleDto().getItems()) {
                sale.getItems().stream()
                        .filter(modelItem -> modelItem.getId().equals(dtoItem.getId()))
                        .findFirst()
                        .ifPresent(modelItem -> {
                            Integer returnedQty = dtoItem.getReturnedQuantity();
                            if (returnedQty != null && returnedQty > 0) {
                                modelItem.setReturnedQuantity(returnedQty);
                                modelItem.setReturnReason(dtoItem.getReturnReason());

                                Integer soldQty = modelItem.getQuantity();
                                if (soldQty != null) {
                                    int remainingQty = soldQty - returnedQty;
                                    modelItem.setQuantity(remainingQty);

                                    if (remainingQty <= 0) {
                                        itemsToRemove.add(modelItem);
                                    }
                                }

                                ProductModel product = productRepository.findByName(modelItem.getProductName());
                                if (product != null) {
                                    int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
                                    product.setQuantity(currentStock + returnedQty);
                                    productRepository.save(product);
                                }
                            }
                        });
            }

            sale.getItems().removeAll(itemsToRemove);
            sale.setTotalAmount(sale.getTotalAmount() - totalRefund);
            saleRepository.save(sale);
            invoiceRepository.save(invoice);

            return "redirect:/invoices/return/success?id=" + id + "&refundAmount=" + totalRefund;

        } catch (Exception e) {
            session.setAttribute("error", "Error processing return: " + e.getMessage());
            return "redirect:/invoices/return/" + id;
        }
    }

    @GetMapping("/return/success")
    public String invoiceReturnSuccessPage(
            @RequestParam Long id,
            @RequestParam Double refundAmount,
            Model model) {

        InvoiceDto invoiceDto = invoiceService.getInvoiceWithItems(id);

        model.addAttribute("invoice", invoiceDto);
        model.addAttribute("refundAmount", refundAmount);
        model.addAttribute("returnedItems", invoiceDto.getSaleDto().getItems());
        model.addAttribute("returnId", id);
        model.addAttribute("returnNotes", invoiceDto.getReturnNotes()); // Optional if saved

        return "invoices/return-confirmation";
    }

    @GetMapping("/sales-report")
    public String salesReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        // If no date range is selected, default to the last 7 days
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Fetch invoices between selected dates
        List<InvoiceModel> invoices = invoiceRepository.findByInvoiceDateTimeBetween(startDate, endDate);

        // Compute total sales and transaction count
        double totalSales = invoices.stream()
                .mapToDouble(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : 0.0)
                .sum();

        int totalTransactions = invoices.size();

        // Add to model
        model.addAttribute("salesReports", invoices);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "invoices/sales-report";
    }

}