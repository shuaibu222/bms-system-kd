package com.shuaibu.controller;

import com.shuaibu.dto.SuspenseDto;
import com.shuaibu.model.SuspenseModel;
import com.shuaibu.repository.SuspenseRepository;
import com.shuaibu.service.SuspenseService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/suspenses")
public class SuspenseController {

    private final SuspenseService SuspenseService;
    private final SuspenseRepository suspenseRepository;

    public SuspenseController(SuspenseService SuspenseService,
                            SuspenseRepository suspenseRepository) {
        this.SuspenseService = SuspenseService;
        this.suspenseRepository = suspenseRepository;
    }

    @GetMapping
    public String listSuspenses(Model model) {
        List<SuspenseModel> suspenses = suspenseRepository.findAll();
        Double totalSuspense = suspenses.stream().mapToDouble(SuspenseModel::getAmount).sum();

        model.addAttribute("suspense", new SuspenseModel());
        model.addAttribute("suspenses", suspenses);
        model.addAttribute("totalSuspenses", totalSuspense);
        return "suspenses/list";
    }

    @PostMapping
    public String saveSuspense(@Valid @ModelAttribute("Suspense") SuspenseDto Suspense,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<SuspenseModel> suspenses = suspenseRepository.findAll();
            Double totalSuspense = suspenses.stream().mapToDouble(SuspenseModel::getAmount).sum();
            
            model.addAttribute("suspenses", suspenses);
            model.addAttribute("totalSuspenses", totalSuspense);
            return "suspenses/list";
        }
        SuspenseService.saveOrUpdateSuspense(Suspense);
        return "redirect:/suspenses?success";
    }

    @GetMapping("/edit/{id}")
    public String editSuspenseForm(@PathVariable Long id, Model model) {
        SuspenseDto Suspense = SuspenseService.getSuspenseById(id);
        model.addAttribute("suspense", Suspense);
        return "suspenses/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSuspense(@PathVariable Long id,
                                @Valid @ModelAttribute("Suspense") SuspenseDto Suspense,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("suspense", Suspense);
            return "suspenses/edit";
        }
        Suspense.setId(id);
        SuspenseService.saveOrUpdateSuspense(Suspense);
        return "redirect:/suspenses?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteSuspense(@PathVariable Long id) {
        SuspenseService.deleteSuspense(id);
        return "redirect:/suspenses?deleted";
    }
}
