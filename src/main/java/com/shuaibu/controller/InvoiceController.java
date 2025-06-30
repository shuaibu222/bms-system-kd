package com.shuaibu.controller;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleDto;
import com.shuaibu.mapper.SaleMapper;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.InvoiceService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;

    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository,
            SaleRepository saleRepository, UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
    }

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
            invoice.setInvoiceValue(quotation.getTotalAmount()); // THIS IS THE KEY LINE

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

}