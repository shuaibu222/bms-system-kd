package com.shuaibu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.SalesReturnDto;
import com.shuaibu.model.SalesReturnModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.SalesReturnRepository;
import com.shuaibu.service.SalesReturnService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/sales-returns")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;
    private final SalesReturnRepository salesReturnRepository;
    private final CustomerRepository customerRepository;

    public SalesReturnController(SalesReturnService salesReturnService, SalesReturnRepository salesReturnRepository,
                                CustomerRepository customerRepository) {
        this.salesReturnService = salesReturnService;
        this.salesReturnRepository = salesReturnRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String listSalesReturns(Model model) {
        model.addAttribute("salesReturn", new SalesReturnModel());
        model.addAttribute("salesReturns", salesReturnRepository.findAll());
        model.addAttribute("customers", customerRepository.findAll());
        return "sales-returns/list";
    }

    @PostMapping
    public String saveSalesReturn(@Valid @ModelAttribute("salesReturn") SalesReturnDto salesReturn,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("salesReturns", salesReturnRepository.findAll());
            model.addAttribute("customers", customerRepository.findAll());
            return "sales-returns/list";
        }
        salesReturnService.saveOrUpdateSalesReturn(salesReturn);
        return "redirect:/sales-returns?success";
    }

    @GetMapping("/edit/{id}")
    public String updateSalesReturnForm(@PathVariable Long id, Model model) {
        SalesReturnDto salesReturn = salesReturnService.getSalesReturnById(id);
        model.addAttribute("salesReturn", salesReturn);
        model.addAttribute("customers", customerRepository.findAll());
        return "sales-returns/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSalesReturn(@PathVariable Long id,
                                    @Valid @ModelAttribute("salesReturn") SalesReturnDto salesReturn,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("salesReturn", salesReturn);
            model.addAttribute("customers", customerRepository.findAll());
            return "sales-returns/edit";
        }
        salesReturn.setId(id);
        salesReturnService.saveOrUpdateSalesReturn(salesReturn);
        return "redirect:/sales-returns?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteSalesReturn(@PathVariable Long id) {
        salesReturnService.deleteSalesReturn(id);
        return "redirect:/sales-returns?error";
    }
}