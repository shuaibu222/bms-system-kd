package com.shuaibu.controller;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.DepositDto;
import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.DepositModel;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.DepositRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.service.CustomerService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final DepositRepository depositRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping
    public String showCustomerList(Model model) {
        List<CustomerDto> customers = customerService.getAllCustomers();

        // Total balance: all balances treated as positive for display
        double totalBalance = customers.stream()
                .mapToDouble(c -> Math.abs(c.getBalance()))
                .sum();

        // Debtors: customers the business owes (balance < 0), show as positive
        double totalDebtors = customers.stream()
                .filter(c -> c.getBalance() < 0)
                .mapToDouble(c -> Math.abs(c.getBalance()))
                .sum();

        // Creditors: customers who owe the business (balance > 0), show as positive
        double totalCreditors = customers.stream()
                .filter(c -> c.getBalance() > 0)
                .mapToDouble(CustomerDto::getBalance)
                .sum();

        model.addAttribute("customers", customers);
        model.addAttribute("customer", new CustomerModel());

        model.addAttribute("totalBalance", totalBalance); // Always positive
        model.addAttribute("totalDebtors", totalDebtors); // Positive
        model.addAttribute("totalCreditors", totalCreditors); // Positive

        return "customers/list";
    }

    @GetMapping("/debtors")
    public String showDebtorsList(Model model) {
        List<CustomerDto> allCustomers = customerService.getAllCustomers();

        // Filter debtors (customers with negative balances)
        List<CustomerDto> debtors = allCustomers.stream()
                .filter(cst -> cst.getBalance() < 0)
                .collect(Collectors.toList());

        // Calculate total debt (sum of negative balances)
        double totalDebt = debtors.stream()
                .mapToDouble(CustomerDto::getBalance)
                .sum();

        model.addAttribute("customers", debtors);
        model.addAttribute("totalDebt", Math.abs(totalDebt));
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

    @GetMapping("/deposits/{id}")
    public String showDepositForm(@PathVariable Long id, Model model) {
        DepositModel deposit = new DepositModel();
        deposit.setId(null);
        deposit.setCustomerId(id);
        model.addAttribute("deposit", deposit);
        return "customers/deposit";
    }

    @PostMapping("/deposits/{id}")
    public String makeDeposit(@PathVariable Long id,
            @ModelAttribute("deposit") @Valid DepositDto depositDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "customers/deposit";
        }

        depositDto.setCustomerId(id);
        customerService.makeDeposit(depositDto);
        return "redirect:/customers?success";
    }

    @GetMapping("/deposits/history/{id}")
    public String showCustomerDeposits(@PathVariable Long id, Model model) {
        CustomerModel customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return "redirect:/customers?error=notfound";
        }

        List<DepositModel> deposits = depositRepository.findByCustomerId(id);

        model.addAttribute("customer", customer);
        model.addAttribute("deposits", deposits);
        return "customers/deposits-history";
    }

    @GetMapping("/invoices/history/{id}")
    public String showCustomerInvoices(@PathVariable Long id, Model model) {
        CustomerModel customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return "redirect:/customers?error=notfound";
        }

        List<InvoiceModel> invoices = invoiceRepository.findByCustomerId(id);

        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoices);
        return "customers/invoices-history";
    }

    @GetMapping("/invoice/{invoiceId}")
    public String viewInvoiceDetail(@PathVariable Long invoiceId, Model model) {
        InvoiceDto invoice = customerService.getInvoiceWithItems(invoiceId); // Service method below

        if (invoice == null) {
            return "redirect:/customers/history/invoices?error=notfound";
        }

        model.addAttribute("invoice", invoice);
        return "customers/invoice-detail";
    }

    @GetMapping("/api/invoices/{id}")
    @ResponseBody
    public ResponseEntity<InvoiceDto> getInvoiceApi(@PathVariable Long id) {
        InvoiceDto invoice = customerService.getInvoiceWithItems(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/ledger")
    public String showCustomerLedger(
            @RequestParam(required = false) String accountNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        List<DepositModel> depositReports = List.of();
        double totalDeposits = 0.0;
        int totalTransactions = 0;
        CustomerModel customer = null;

        model.addAttribute("accountNo", accountNo);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        if (accountNo != null && !accountNo.isBlank()) {
            try {
                customer = customerRepository.findByPhone(accountNo).get();
                if (customer != null) {
                    Long customerId = customer.getId();

                    LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
                    LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

                    if (start != null && end != null) {
                        depositReports = depositRepository.findByCustomerIdAndDepositDateBetween(customerId, start,
                                end);
                    } else {
                        depositReports = depositRepository.findByCustomerId(customerId);
                    }

                    totalDeposits = depositReports.stream().mapToDouble(DepositModel::getTotalAmount).sum();
                    totalTransactions = depositReports.size();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("depositReports", depositReports);
        model.addAttribute("totalDeposits", totalDeposits);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("customer", customer);

        return "customers/ledger";
    }

    @GetMapping("/payments-report")
    public String getPaymentsReport(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<DepositModel> deposits = depositRepository.findByDepositDateBetween(startDate, endDate);

        double totalSales = deposits.stream()
                .mapToDouble(dep -> dep.getTotalAmount() != null ? dep.getTotalAmount() : 0.0)
                .sum();

        int totalTransactions = deposits.size();

        // Enrich each deposit with customer name
        List<Map<String, Object>> enrichedReports = deposits.stream().map(deposit -> {
            Map<String, Object> map = new HashMap<>();
            map.put("deposit", deposit);

            CustomerModel customer = customerRepository.findById(deposit.getCustomerId()).orElse(null);
            map.put("customerName", customer != null ? customer.getName() : "Unknown");

            return map;
        }).collect(Collectors.toList());

        model.addAttribute("salesReports", enrichedReports);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "customers/payments-report";
    }

}