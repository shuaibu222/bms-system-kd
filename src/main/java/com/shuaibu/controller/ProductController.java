package com.shuaibu.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.LowStockDto;
import com.shuaibu.dto.ProductDto;
import com.shuaibu.model.LowStockModel;
import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final LowStockRepository lowStockRepository;

    @GetMapping
    public String listProducts(Model model) {
        List<ProductModel> products = productRepository.findAll();

        double totalProductValue = products.stream()
                .mapToDouble(p -> {
                    Integer qty = p.getQuantity() != null ? p.getQuantity() : 0;
                    Double price = p.getPrice() != null ? p.getPrice() : 0.0;
                    return qty * price;
                })
                .sum();

        model.addAttribute("product", new ProductModel());
        model.addAttribute("products", products);
        model.addAttribute("totalProductValue", totalProductValue);
        return "products/list";
    }

    @PostMapping
    public String saveProduct(
            @Valid @ModelAttribute("product") ProductDto product,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findAll());
            return "products/list";
        }
        productService.saveOrUpdateProduct(product);
        return "redirect:/products?success";
    }

    @GetMapping("/edit/{id}")
    public String updateProductForm(@PathVariable Long id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/edit";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDto product,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "products/edit";
        }
        product.setId(id);
        productService.saveOrUpdateProduct(product);
        return "redirect:/products?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products?error";
    }

    // 🔻 LOW STOCK

    @GetMapping("/lowstocks")
    public String listLowStockProducts(Model model) {
        List<ProductModel> lowStockProducts = productRepository.findAll().stream()
                .filter(p -> {
                    Integer qty = p.getQuantity();
                    Integer alert = p.getLowQuantityAlert();
                    return qty != null && alert != null && qty <= alert;
                })
                .toList();

        model.addAttribute("products", lowStockProducts);
        return "lowstocks/list";
    }

    // 🔻 PURCHASE FORM

    @GetMapping("/purchases/update/{id}")
    public String showPurchaseForm(@PathVariable Long id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "lowstocks/edit";
    }

    @PostMapping("/purchases/update/{id}")
    public String handlePurchaseUpdate(@PathVariable Long id,
            @ModelAttribute("lowStockDto") @Valid LowStockDto dto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("lowStockDto", dto);
            return "lowstocks/edit";
        }

        productService.handlePurchaseAndUpdateProduct(id, dto);
        return "redirect:/products/lowstocks?purchaseSuccess";
    }

    // 🔻 PURCHASE REPORT

    @GetMapping("/purchases-report")
    public String getPurchaseReport(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<LowStockModel> purchases = lowStockRepository.findByLowStockDateBetween(startDate, endDate);

        double totalPurchases = purchases.stream()
                .mapToDouble(p -> {
                    Integer qty = p.getQuantity();
                    Double price = p.getPrice();
                    return (qty != null && price != null) ? qty * price : 0.0;
                })
                .sum();

        model.addAttribute("purchaseReports", purchases);
        model.addAttribute("totalPurchases", totalPurchases);
        model.addAttribute("totalTransactions", purchases.size());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "lowstocks/purchases-report";
    }

}
