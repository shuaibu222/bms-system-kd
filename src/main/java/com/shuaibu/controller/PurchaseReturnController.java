package com.shuaibu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.PurchaseReturnDto;
import com.shuaibu.model.PurchaseReturnModel;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.PurchaseReturnRepository;
import com.shuaibu.service.PurchaseReturnService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/purchase-returns")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;
    private final PurchaseReturnRepository purchaseReturnRepository;
    private final ProductRepository productRepository;

    public PurchaseReturnController(PurchaseReturnService purchaseReturnService,
            PurchaseReturnRepository purchaseReturnRepository,
            ProductRepository productRepository) {
        this.purchaseReturnService = purchaseReturnService;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listPurchaseReturns(Model model) {
        model.addAttribute("purchaseReturn", new PurchaseReturnModel());
        model.addAttribute("purchaseReturns", purchaseReturnRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "purchase-returns/list";
    }

    @PostMapping
    public String savePurchaseReturn(@Valid @ModelAttribute("purchaseReturn") PurchaseReturnDto purchaseReturn,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("purchaseReturns", purchaseReturnRepository.findAll());

            model.addAttribute("products", productRepository.findAll());

            return "purchase-returns/list";
        }
        purchaseReturnService.saveOrUpdatePurchaseReturn(purchaseReturn);
        return "redirect:/purchase-returns?success";
    }

    @GetMapping("/edit/{id}")
    public String updatePurchaseReturnForm(@PathVariable Long id, Model model) {
        PurchaseReturnDto purchaseReturn = purchaseReturnService.getPurchaseReturnById(id);
        model.addAttribute("purchaseReturn", purchaseReturn);
        model.addAttribute("products", productRepository.findAll());
        return "purchase-returns/edit";
    }

    @PostMapping("/update/{id}")
    public String updatePurchaseReturn(@PathVariable Long id,
            @Valid @ModelAttribute("purchaseReturn") PurchaseReturnDto purchaseReturn,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("purchaseReturn", purchaseReturn);

            model.addAttribute("products", productRepository.findAll());

            return "purchase-returns/edit";
        }
        purchaseReturn.setId(id);
        purchaseReturnService.saveOrUpdatePurchaseReturn(purchaseReturn);
        return "redirect:/purchase-returns?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deletePurchaseReturn(@PathVariable Long id) {
        purchaseReturnService.deletePurchaseReturn(id);
        return "redirect:/purchase-returns?error";
    }
}