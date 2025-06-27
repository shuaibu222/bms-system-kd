package com.shuaibu.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.PurchaseRepository;
import com.shuaibu.repository.SaleRepository;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public DashboardController(SaleRepository saleRepository, PurchaseRepository purchaseRepository, 
    ProductRepository productRepository, CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("userModel", authentication.getName());
        } else {
            model.addAttribute("userModel", "Guest"); // Default text if not logged in
        }        
        model.addAttribute("totalSales", saleRepository.count());
        model.addAttribute("totalPurchases", purchaseRepository.count());
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());
        // todays sales
        model.addAttribute("todaysSales", saleRepository.findBySaleDateTimeBetween(LocalDate.now(), LocalDate.now()));
        return "dashboard/dashboard";
    }

}
