package com.shuaibu.controller;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.DepositDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.DepositModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.DepositRepository;
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
    private final CustomerRepository customerRepository;
    private final DepositRepository depositRepository;

    public CustomerController(CustomerService customerService, SaleRepository saleRepository,
            CustomerRepository customerRepository, DepositRepository depositRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.depositRepository = depositRepository;
    }

    @GetMapping
    public String showCustomerList(Model model) {
        List<CustomerDto> customers = customerService.getAllCustomers();

        double totalBalance = customers.stream().mapToDouble(CustomerDto::getBalance).sum();
        double totalDebtors = customers.stream().filter(c -> c.getBalance() >= 0).mapToDouble(CustomerDto::getBalance)
                .sum();
        double totalCreditors = customers.stream().filter(c -> c.getBalance() < 0).mapToDouble(CustomerDto::getBalance)
                .sum();

        model.addAttribute("customers", customers);
        model.addAttribute("customer", new CustomerModel());

        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebtors", totalDebtors);
        model.addAttribute("totalCreditors", totalCreditors);

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

    @GetMapping("/deposits/{id}")
    public String showDepositForm(@PathVariable Long id, Model model) {
        DepositModel deposit = new DepositModel();
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

}