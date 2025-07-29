package com.shuaibu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.model.SaleModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.SaleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

    @GetMapping("/search-customers")
    @ResponseBody
    public List<Map<String, String>> searchCustomers(@RequestParam("query") String query) {
        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query)
                .stream()
                .map(customer -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", customer.getName());
                    map.put("phone", customer.getPhone());
                    return map;
                })
                .collect(Collectors.toList());
    }

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
        latestSale.setSalesAgent(userModel.getFullName());

        return latestSale;
    }

    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return "redirect:/sales/list?deleteSuccess";
    }
}
