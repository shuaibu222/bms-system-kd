package com.shuaibu.controller;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String showCustomerList(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("customer", new CustomerModel());
        return "customers/list";
    }

    @PostMapping
    public String saveCustomer(@Valid @ModelAttribute("customer") CustomerDto customerDto, BindingResult result, Model model) {
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
    public String updateCustomer(@PathVariable Long id, @Valid @ModelAttribute("customer") CustomerDto customerDto, BindingResult result) {
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
}