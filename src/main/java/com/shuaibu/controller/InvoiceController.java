package com.shuaibu.controller;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleItemDto;
import com.shuaibu.model.*;
import com.shuaibu.repository.*;
import com.shuaibu.service.InvoiceService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

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

    // --- Invoice Listing and Creation ---

    @GetMapping
    public String showCreateInvoicePage(Model model) {
        model.addAttribute("invoice", new InvoiceModel());
        return "invoices/list";
    }

    @GetMapping("/list")
    public String listAllInvoices(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAll());
        return "invoices/list-invoices";
    }

    @GetMapping("/search")
    public String findByQtnNum(@RequestParam String qtnNum, Model model) {
        UserModel user = getCurrentUser();
        SaleModel quote = saleRepository.findByQtnNum(qtnNum);

        if (quote != null) {
            InvoiceModel invoice = new InvoiceModel();
            invoice.setInvoiceValue(quote.getTotalAmount());

            model.addAttribute("quotation", quote);
            model.addAttribute("invoice", invoice);
            model.addAttribute("agent", user.getFullName());
        }
        return "invoices/list";
    }

    @PostMapping
    public String createInvoice(@Valid @ModelAttribute("invoice") InvoiceDto invoiceDto,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("invoice", invoiceDto);
            return "invoices/list";
        }

        try {
            invoiceService.saveOrUpdateInvoice(invoiceDto);
            return "redirect:/invoices/success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "invoices/list";
        }
    }

    @GetMapping("/success")
    public String invoiceSuccess() {
        return "invoices/invoice-success";
    }

    // --- Invoice Edit/Delete ---

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.getInvoiceById(id));
        return "invoices/edit";
    }

    @PostMapping("/update/{id}")
    public String updateInvoice(@PathVariable Long id,
            @Valid @ModelAttribute("invoice") InvoiceDto invoiceDto,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("invoice", invoiceDto);
            return "invoices/edit";
        }
        invoiceDto.setId(id);
        invoiceService.saveOrUpdateInvoice(invoiceDto);
        return "redirect:/invoices?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return "redirect:/invoices/list?deleteSuccess";
    }

    // --- API ---

    @GetMapping("/latest")
    @ResponseBody
    public ResponseEntity<?> getLatestInvoice() {
        try {
            InvoiceDto latest = invoiceService.getLatestInvoice();
            latest.setCashierAgent(getCurrentUser().getFullName());
            return ResponseEntity.ok(latest);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Sales Return Handling ---

    @GetMapping("/sales-return/search")
    public String searchForReturn(@RequestParam(required = false) String invNum,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        InvoiceModel invoice = null;

        if (invNum != null && !invNum.isBlank()) {
            invoice = invoiceRepository.findByInvNum(invNum);
        } else if (phone != null && !phone.isBlank() && date != null) {
            CustomerModel customer = customerRepository.findByPhone(phone).orElse(null);
            if (customer != null) {
                invoice = invoiceRepository.findFirstByCustomerIdAndInvoiceDateTime(customer.getId(), date);
            }
        } else if (date != null) {
            model.addAttribute("invoices", invoiceRepository.findByInvoiceDateTime(date));
            return "invoices/sales-return-list";
        }

        if (invoice != null) {
            return showReturnForm(invoice.getId(), model);
        }

        model.addAttribute("error", "No matching invoice found.");
        return "invoices/sales-return";
    }

    @GetMapping("/sales-return/process/{id}")
    public String showReturnForm(@PathVariable Long id, Model model) {
        InvoiceDto invoice = invoiceService.getInvoiceWithItems(id);

        invoice.getSaleDto().getItems().forEach(item -> {
            item.setReturnedQuantity(0);
            item.setReturnReason(null);
        });

        model.addAttribute("invoice", invoice);
        return "invoices/sales-return-process";
    }

    @PostMapping("/process-return/{id}")
    public String processReturn(@PathVariable Long id,
            @ModelAttribute("invoice") InvoiceDto invoiceDto,
            HttpSession session) {
        try {
            double totalRefund = 0.0;
            List<SaleItemDto> items = invoiceDto.getSaleDto().getItems();

            InvoiceModel invoice = invoiceRepository.findById(id).orElseThrow();
            SaleModel sale = saleRepository.findById(invoice.getQuotationId()).orElseThrow();
            List<SaleItemModel> toRemove = new ArrayList<>();

            for (SaleItemDto dto : items) {
                if (dto.getReturnedQuantity() != null && dto.getReturnedQuantity() > 0) {
                    totalRefund += dto.getReturnedQuantity() * dto.getPrice();

                    SaleItemModel itemModel = sale.getItems().stream()
                            .filter(i -> i.getId().equals(dto.getId()))
                            .findFirst().orElse(null);

                    if (itemModel != null) {
                        itemModel.setReturnedQuantity(dto.getReturnedQuantity());
                        itemModel.setReturnReason(dto.getReturnReason());

                        int remainingQty = itemModel.getQuantity() - dto.getReturnedQuantity();
                        itemModel.setQuantity(remainingQty);

                        if (remainingQty <= 0) {
                            toRemove.add(itemModel);
                        }

                        ProductModel product = productRepository.findByName(itemModel.getProductName());
                        if (product != null) {
                            product.setQuantity(
                                    Optional.ofNullable(product.getQuantity()).orElse(0) + dto.getReturnedQuantity());
                            productRepository.save(product);
                        }
                    }
                }
            }

            sale.getItems().removeAll(toRemove);
            sale.setTotalAmount(sale.getTotalAmount() - totalRefund);
            invoice.setTotalAmount(invoice.getTotalAmount() - totalRefund);
            invoice.setInvoiceValue(invoice.getInvoiceValue() - totalRefund);

            saleRepository.save(sale);
            invoiceRepository.save(invoice);

            return "redirect:/invoices/return/success?id=" + id + "&refundAmount=" + totalRefund;

        } catch (Exception e) {
            session.setAttribute("error", "Return failed: " + e.getMessage());
            return "redirect:/invoices/return/" + id;
        }
    }

    @GetMapping("/return/success")
    public String returnSuccess(@RequestParam Long id, @RequestParam Double refundAmount, Model model) {
        InvoiceDto invoice = invoiceService.getInvoiceWithItems(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("refundAmount", refundAmount);
        model.addAttribute("returnedItems", invoice.getSaleDto().getItems());
        model.addAttribute("returnId", id);
        model.addAttribute("returnNotes", invoice.getReturnNotes());
        return "invoices/return-confirmation";
    }

    // --- Reporting ---

    @GetMapping("/sales-report")
    public String salesReport(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<InvoiceModel> invoices = invoiceRepository.findByInvoiceDateTimeBetween(startDate, endDate);

        double totalSales = invoices.stream()
                .mapToDouble(inv -> Optional.ofNullable(inv.getTotalAmount()).orElse(0.0))
                .sum();

        model.addAttribute("salesReports", invoices);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalTransactions", invoices.size());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "invoices/sales-report";
    }

    // --- Utility ---

    private UserModel getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }
}
