package com.shuaibu.controller;

import com.shuaibu.dto.LoanDto;
import com.shuaibu.model.LoanModel;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final StaffRepository staffRepository;

    @GetMapping
    public String listLoans(Model model) {
        populateLoanPageData(model, new LoanModel());
        return "staff-loans/list";
    }

    @PostMapping
    public String saveLoan(@Valid @ModelAttribute("loan") LoanDto loan,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            populateLoanPageData(model, loan);
            return "staff-loans/list";
        }

        loanService.saveOrUpdateLoan(loan);
        return "redirect:/loans?success";
    }

    @GetMapping("/edit/{id}")
    public String editLoanForm(@PathVariable Long id, Model model) {
        LoanDto loan = loanService.getLoanById(id);
        model.addAttribute("loan", loan);
        model.addAttribute("staffList", staffRepository.findAll());
        return "staff-loans/edit";
    }

    @PostMapping("/update/{id}")
    public String updateLoan(@PathVariable Long id,
            @Valid @ModelAttribute("loan") LoanDto loan,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("loan", loan);
            model.addAttribute("staffList", staffRepository.findAll());
            return "staff-loans/edit";
        }

        loan.setId(id);
        loanService.saveOrUpdateLoan(loan);
        return "redirect:/loans?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return "redirect:/loans?deleted";
    }

    // Helper to reduce repetition
    private void populateLoanPageData(Model model, Object loanObject) {
        List<LoanModel> loans = loanRepository.findAll();

        double totalLoans = loans.stream()
                .mapToDouble(l -> l.getAmount() != null ? l.getAmount() : 0.0)
                .sum();

        double totalRepaid = loans.stream()
                .mapToDouble(l -> l.getAmountRepaid() != null ? l.getAmountRepaid() : 0.0)
                .sum();

        double totalOutstanding = totalLoans - totalRepaid;

        model.addAttribute("loan", loanObject);
        model.addAttribute("loans", loans);
        model.addAttribute("totalLoans", totalLoans);
        model.addAttribute("totalRepaid", totalRepaid);
        model.addAttribute("totalOutstanding", totalOutstanding);
        model.addAttribute("staffList", staffRepository.findAll());
    }
}
