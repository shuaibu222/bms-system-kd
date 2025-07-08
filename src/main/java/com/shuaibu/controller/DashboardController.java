package com.shuaibu.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.LowStockModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final LowStockRepository lowStockRepository;

    @GetMapping
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = (auth != null && auth.isAuthenticated() &&
                !"anonymousUser".equals(auth.getPrincipal()))
                        ? auth.getName()
                        : "Guest";
        model.addAttribute("userModel", username);

        // Today's sales (start and end of day)
        LocalDate startOfDay = LocalDate.now();
        LocalDate endOfDay = LocalDate.now();

        List<LowStockModel> purchases = lowStockRepository.findByLowStockDateBetween(startOfDay, endOfDay);
        List<InvoiceModel> invoices = invoiceRepository.findByInvoiceDateTimeBetween(startOfDay, endOfDay);
        model.addAttribute("totalPurchases", purchases.size());
        model.addAttribute("totalSales", invoices.size());
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());

        return "dashboard/dashboard";
    }
}
