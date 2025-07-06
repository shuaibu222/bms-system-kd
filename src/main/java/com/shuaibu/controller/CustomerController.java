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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final DepositRepository depositRepository;
    private final InvoiceRepository invoiceRepository;

    // ----------------- LIST CUSTOMERS ------------------
    @GetMapping
    public String showCustomerList(Model model) {
        List<CustomerDto> customers = customerService.getAllCustomers();

        double totalBalance = customers.stream().mapToDouble(c -> Math.abs(c.getBalance())).sum();
        double totalDebtors = customers.stream().filter(c -> c.getBalance() < 0)
                .mapToDouble(c -> Math.abs(c.getBalance())).sum();
        double totalCreditors = customers.stream().filter(c -> c.getBalance() > 0).mapToDouble(CustomerDto::getBalance)
                .sum();

        model.addAttribute("customers", customers);
        model.addAttribute("customer", new CustomerDto());
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebtors", totalDebtors);
        model.addAttribute("totalCreditors", totalCreditors);
        return "customers/list";
    }

    // ----------------- LIST DEBTORS ------------------
    @GetMapping("/debtors")
    public String showDebtorsList(Model model) {
        List<CustomerDto> debtors = customerService.getAllCustomers().stream()
                .filter(c -> c.getBalance() < 0)
                .collect(Collectors.toList());

        double totalDebt = debtors.stream().mapToDouble(CustomerDto::getBalance).sum();

        model.addAttribute("customers", debtors);
        model.addAttribute("totalDebt", Math.abs(totalDebt));
        return "customers/debtors";
    }

    // ----------------- CREATE CUSTOMER ------------------
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

    // ----------------- EDIT CUSTOMER ------------------
    @GetMapping("/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customers/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Long id,
            @Valid @ModelAttribute("customer") CustomerDto customerDto,
            BindingResult result) {
        if (result.hasErrors()) {
            return "customers/edit";
        }
        customerDto.setId(id);
        customerService.saveOrUpdateCustomer(customerDto);
        return "redirect:/customers?updateSuccess";
    }

    // ----------------- DELETE CUSTOMER ------------------
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers?error";
    }

    // ----------------- DEPOSIT FOR CUSTOMER ------------------
    @GetMapping("/deposits/{id}")
    public String showDepositForm(@PathVariable Long id, Model model) {
        DepositModel deposit = new DepositModel();
        deposit.setCustomerId(id);
        model.addAttribute("deposit", deposit);
        return "customers/deposit";
    }

    @PostMapping("/deposits/{id}")
    public String makeDeposit(@PathVariable Long id,
            @Valid @ModelAttribute("deposit") DepositDto depositDto,
            BindingResult result) {
        if (result.hasErrors()) {
            return "customers/deposit";
        }
        depositDto.setCustomerId(id);
        customerService.makeDeposit(depositDto);
        return "redirect:/customers?success";
    }

    // ----------------- DEPOSIT HISTORY ------------------
    @GetMapping("/deposits/history/{id}")
    public String showCustomerDeposits(@PathVariable Long id, Model model) {
        CustomerModel customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
            return "redirect:/customers?error=notfound";

        List<DepositModel> deposits = depositRepository.findByCustomerId(id);
        model.addAttribute("customer", customer);
        model.addAttribute("deposits", deposits);
        return "customers/deposits-history";
    }

    // ----------------- INVOICE HISTORY ------------------
    @GetMapping("/invoices/history/{id}")
    public String showCustomerInvoices(@PathVariable Long id, Model model) {
        CustomerModel customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
            return "redirect:/customers?error=notfound";

        List<InvoiceModel> invoices = invoiceRepository.findByCustomerId(id);
        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoices);
        return "customers/invoices-history";
    }

    // ----------------- INVOICE DETAIL ------------------
    @GetMapping("/invoice/{invoiceId}")
    public String viewInvoiceDetail(@PathVariable Long invoiceId, Model model) {
        InvoiceDto invoice = customerService.getInvoiceWithItems(invoiceId);
        if (invoice == null)
            return "redirect:/customers/history/invoices?error=notfound";

        model.addAttribute("invoice", invoice);
        return "customers/invoice-detail";
    }

    // ----------------- INVOICE API ------------------
    @GetMapping("/api/invoices/{id}")
    @ResponseBody
    public ResponseEntity<InvoiceDto> getInvoiceApi(@PathVariable Long id) {
        InvoiceDto invoice = customerService.getInvoiceWithItems(id);
        return invoice == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(invoice);
    }

    // ----------------- CUSTOMER LEDGER ------------------
    @GetMapping("/ledger")
    public String showCustomerLedger(@RequestParam(required = false) String accountNo,
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
            customer = customerRepository.findByPhone(accountNo).orElse(null);
            if (customer != null) {
                Long customerId = customer.getId();
                LocalDate start = parseDate(startDate);
                LocalDate end = parseDate(endDate);

                depositReports = (start != null && end != null)
                        ? depositRepository.findByCustomerIdAndDepositDateBetween(customerId, start, end)
                        : depositRepository.findByCustomerId(customerId);

                totalDeposits = depositReports.stream()
                        .mapToDouble(d -> d.getTotalAmount() != null ? d.getTotalAmount() : 0.0).sum();
                totalTransactions = depositReports.size();
            }
        }

        model.addAttribute("depositReports", depositReports);
        model.addAttribute("totalDeposits", totalDeposits);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("customer", customer);

        return "customers/ledger";
    }

    // ----------------- PAYMENTS REPORT ------------------
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

        List<Map<String, Object>> enrichedReports = deposits.stream().map(dep -> {
            Map<String, Object> map = new HashMap<>();
            map.put("deposit", dep);
            CustomerModel customer = customerRepository.findById(dep.getCustomerId()).orElse(null);
            map.put("customerName", customer != null ? customer.getName() : "Unknown");
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("salesReports", enrichedReports);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalTransactions", deposits.size());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "customers/payments-report";
    }

    private LocalDate parseDate(String raw) {
        try {
            return (raw != null && !raw.isBlank()) ? LocalDate.parse(raw) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
