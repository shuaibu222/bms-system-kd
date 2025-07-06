package com.shuaibu.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = (auth != null && auth.isAuthenticated() &&
                !"anonymousUser".equals(auth.getPrincipal()))
                        ? auth.getName()
                        : "Guest";
        model.addAttribute("userModel", username);

        // Total metrics
        model.addAttribute("totalSales", saleRepository.count());
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());

        // Today's sales (start and end of day)
        LocalDate startOfDay = LocalDate.now();
        LocalDate endOfDay = LocalDate.now();

        model.addAttribute("todaysSales",
                saleRepository.findBySaleDateTimeBetween(startOfDay, endOfDay));

        return "dashboard/dashboard";
    }
}
