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

    private final SuspenseService suspenseService;
    private final SuspenseRepository suspenseRepository;

    public SuspenseController(SuspenseService suspenseService,
            SuspenseRepository suspenseRepository) {
        this.suspenseService = suspenseService;
        this.suspenseRepository = suspenseRepository;
    }

    @GetMapping
    public String listSuspenses(Model model) {
        List<SuspenseModel> suspenses = suspenseRepository.findAll();
        double totalSuspense = suspenses.stream().mapToDouble(SuspenseModel::getAmount).sum();

        model.addAttribute("suspense", new SuspenseModel());
        model.addAttribute("suspenses", suspenses);
        model.addAttribute("totalSuspenses", totalSuspense);
        return "suspenses/list";
    }

    @PostMapping
    public String saveSuspense(@Valid @ModelAttribute("suspense") SuspenseDto suspenseDto,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<SuspenseModel> suspenses = suspenseRepository.findAll();
            double totalSuspense = suspenses.stream().mapToDouble(SuspenseModel::getAmount).sum();

            model.addAttribute("suspenses", suspenses);
            model.addAttribute("totalSuspenses", totalSuspense);
            return "suspenses/list";
        }
        suspenseService.saveOrUpdateSuspense(suspenseDto);
        return "redirect:/suspenses?success";
    }

    @GetMapping("/edit/{id}")
    public String editSuspenseForm(@PathVariable Long id, Model model) {
        SuspenseDto suspenseDto = suspenseService.getSuspenseById(id);
        model.addAttribute("suspense", suspenseDto);
        return "suspenses/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSuspense(@PathVariable Long id,
            @Valid @ModelAttribute("suspense") SuspenseDto suspenseDto,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("suspense", suspenseDto);
            return "suspenses/edit";
        }
        suspenseDto.setId(id);
        suspenseService.saveOrUpdateSuspense(suspenseDto);
        return "redirect:/suspenses?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteSuspense(@PathVariable Long id) {
        suspenseService.deleteSuspense(id);
        return "redirect:/suspenses?deleted";
    }
}
