package com.shuaibu.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.model.SaleModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.SaleService;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleController(SaleService saleService, SaleRepository saleRepository,
            CustomerRepository customerRepository, ProductRepository productRepository,
            InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listSalesForm(Model model) {
        model.addAttribute("sale", new SaleModel());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "sales/list";
    }

    @GetMapping("/list")
    public String listSales(Model model) {
        model.addAttribute("sales", saleRepository.findAll());
        return "sales/list-sales";
    }

    // Checkout code
    @PostMapping("/checkout")
    public String checkout(@RequestBody SaleDto saleDto) {
        saleService.saveOrUpdateSale(saleDto);
        return "redirect:/sales/success";
    }

    @GetMapping("/success")
    public String saleSuccessPage() {
        return "sales/sales-success";
    }

    @GetMapping("/latest")
    @ResponseBody
    public SaleDto getLatestSale() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel userModel = userRepository.findByUsername(authentication.getName());

        SaleDto latestSale = saleService.getLatestSale();
        latestSale.setSalesAgent(userModel.getFullName()); // Assuming fullName exists

        return latestSale;
    }

    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return "redirect:/sales/list?deleteSuccess";
    }
}