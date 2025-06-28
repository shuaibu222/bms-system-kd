package com.shuaibu.controller;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.SaleRepository;
import com.shuaibu.service.CustomerService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final SaleRepository saleRepository;

    public CustomerController(CustomerService customerService, SaleRepository saleRepository) {
        this.customerService = customerService;
        this.saleRepository = saleRepository;
    }

    @GetMapping
    public String showCustomerList(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("customer", new CustomerModel());
        return "customers/list";
    }

    @GetMapping("/debtors")
    public String showDebtorsList(Model model) {
        List<CustomerDto> allCustomers = customerService.getAllCustomers();

        // Filter debtors (customers with negative balances)
        List<CustomerDto> debtors = allCustomers.stream()
                .filter(cst -> cst.getBalance() > 0)
                .collect(Collectors.toList());

        // Calculate total debt (sum of negative balances)
        double totalDebt = debtors.stream()
                .mapToDouble(CustomerDto::getBalance)
                .sum();

        model.addAttribute("customers", debtors);
        model.addAttribute("totalDebt", totalDebt);
        return "customers/debtors";
    }

    @PostMapping
    public String saveCustomer(@Valid @ModelAttribute("customer") CustomerDto customerDto, BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            return "customers/list";
        }
        customerService.saveOrUpdateCustomer(customerDto);
        return "redirect:/customers?success";
    }

    @GetMapping("/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customers/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Long id, @Valid @ModelAttribute("customer") CustomerDto customerDto,
            BindingResult result) {
        if (result.hasErrors()) {
            return "customers/edit";
        }
        customerDto.setId(id);
        customerService.saveOrUpdateCustomer(customerDto);
        return "redirect:/customers?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers?error";
    }

    @GetMapping("/ledger")
    public String showCustomerLedger(
            @RequestParam(required = false) String accountNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        List<SaleModel> salesReports = List.of();
        double totalSales = 0.0;
        int totalTransactions = 0;
        String customerName = null;

        model.addAttribute("accountNo", accountNo);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        if (accountNo != null && !accountNo.isBlank()) {
            try {
                Integer phone = Integer.parseInt(accountNo);

                LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
                LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

                if (start != null && end != null) {
                    salesReports = saleRepository.findByPhoneAndSaleDateTimeBetween(phone, start, end);
                }

                totalSales = salesReports.stream().mapToDouble(SaleModel::getTotalAmount).sum();
                totalTransactions = salesReports.size();

                if (!salesReports.isEmpty()) {
                    customerName = salesReports.get(0).getCustomerName();
                }

            } catch (NumberFormatException e) {
                System.out.println(e);
            }
        }

        model.addAttribute("salesReports", salesReports);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("customer", customerName != null ? customerName : null);

        return "customers/ledger";
    }
}