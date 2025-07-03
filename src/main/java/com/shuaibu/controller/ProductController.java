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
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final LowStockRepository lowStockRepository;

    public ProductController(ProductService productService,
            ProductRepository productRepository,
            ExpenseRepository expenseRepository,
            InvoiceRepository invoiceRepository,
            LowStockRepository lowStockRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.lowStockRepository = lowStockRepository;
    }

    @GetMapping
    public String listProducts(Model model) {
        List<ProductModel> products = productRepository.findAll();

        // Calculate total inventory value
        double totalProductValue = products.stream()
                .mapToDouble(p -> (p.getQuantity() != null ? p.getQuantity() : 0) *
                        (p.getPrice() != null ? p.getPrice() : 0.0))
                .sum();

        model.addAttribute("product", new ProductModel());
        model.addAttribute("products", products);
        model.addAttribute("totalProductValue", totalProductValue); // Add total

        return "products/list";
    }

    @PostMapping
    public String saveProduct(@Valid @ModelAttribute("product") ProductDto product,
            BindingResult result, Model model) {
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
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "products/edit";
        }
        product.setId(id);
        productService.saveOrUpdateProduct(product);
        return "redirect:/products?updateSuccess";
    }

    @GetMapping("/stock/edit/{id}")
    public String updateStockForm(@PathVariable Long id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/edit-stock";
    }

    @PostMapping("/stock/update/{id}")
    public String updateStock(@PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDto product,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "products/edit-stock";
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

    // Low Stock Controller
    @GetMapping("/lowstocks")
    public String listLowStockProducts(Model model) {
        List<ProductModel> productModels = productRepository.findAll().stream()
                .filter(p -> p.getQuantity() <= p.getLowQuantityAlert())
                .toList();

        model.addAttribute("products", productModels);
        return "lowstocks/list";
    }

    @GetMapping("/lowstocks/edit/{id}")
    public String updateLowStockProductForm(@PathVariable Long id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "lowstocks/edit";
    }

    @PostMapping("/lowstocks/update/{id}")
    public String updateLowStockProduct(@PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDto product,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "lowstocks/edit";
        }
        product.setId(id);
        productService.saveOrUpdateProduct(product);
        return "redirect:/products/lowstocks?success";
    }

    @GetMapping("/purchases/update/{id}")
    public String showPurchaseForm(@PathVariable Long id, Model model) {
        ProductDto product = productService.getProductById(id); // ✅ fetch product
        model.addAttribute("product", product); // map to form dto
        return "purchases/edit";
    }

    @PostMapping("/purchases/update/{id}")
    public String handlePurchaseUpdate(@PathVariable Long id,
            @ModelAttribute("lowStockDto") @Valid LowStockDto dto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("lowStockDto", dto);
            return "purchases/edit";
        }

        productService.handlePurchaseAndUpdateProduct(id, dto); // ✅ combined logic
        return "redirect:/products/lowstocks?purchaseSuccess";
    }

    @GetMapping("/purchases-report")
    public String getPurchaseReport(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        // Default to last 7 days if dates not specified
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Fetch all purchases (low stock entries) in range
        List<LowStockModel> purchases = lowStockRepository.findByLowStockDateBetween(startDate, endDate);

        // Calculate total purchases (sum of price * quantity)
        double totalPurchases = purchases.stream()
                .mapToDouble(
                        p -> (p.getPrice() != null && p.getQuantity() != null) ? p.getPrice() * p.getQuantity() : 0.0)
                .sum();

        int totalTransactions = purchases.size();

        model.addAttribute("purchaseReports", purchases);
        model.addAttribute("totalPurchases", totalPurchases);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "lowstocks/purchases-report";
    }

}