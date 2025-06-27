package com.shuaibu.controller;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.InvoiceService;
import com.shuaibu.service.SaleService;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public SaleController(SaleService saleService, SaleRepository saleRepository,
            CustomerRepository customerRepository, ProductRepository productRepository,
            InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
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

    @GetMapping("/reports")
    public String getSalesReport(Model model) {
        return "reports/sales/list";
    }

    @GetMapping("/reports/{startDate}/{endDate}")
    public String getInvoiceReport(
            @PathVariable(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @PathVariable(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<InvoiceModel> salesReports = invoiceRepository.findByInvoiceDateTimeBetween(startDate, endDate);

        if (salesReports.isEmpty()) {
            System.out.println("No sales data found for the given range.");
        }

        // Define a DecimalFormat instance for one decimal place
        DecimalFormat df = new DecimalFormat("#.#");

        double totalSales = salesReports.stream().mapToDouble(InvoiceModel::getTotalAmount).sum();
        long totalTransactions = salesReports.size();
        OptionalDouble maxSales = salesReports.stream().mapToDouble(InvoiceModel::getTotalAmount).max();
        OptionalDouble minSales = salesReports.stream().mapToDouble(InvoiceModel::getTotalAmount).min();
        OptionalDouble avgSales = salesReports.stream().mapToDouble(InvoiceModel::getTotalAmount).average();

        model.addAttribute("salesReports", salesReports);
        model.addAttribute("totalSales", Double.parseDouble(df.format(totalSales)));
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("maxSales",
                maxSales.isPresent() ? Double.parseDouble(df.format(maxSales.getAsDouble())) : 0.0);
        model.addAttribute("minSales",
                minSales.isPresent() ? Double.parseDouble(df.format(minSales.getAsDouble())) : 0.0);
        model.addAttribute("avgSales",
                avgSales.isPresent() ? Double.parseDouble(df.format(avgSales.getAsDouble())) : 0.0);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reports/sales/list"; // Ensure this template exists
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