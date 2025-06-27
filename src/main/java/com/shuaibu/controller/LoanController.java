package com.shuaibu.controller;

import com.shuaibu.dto.LoanDto;
import com.shuaibu.model.LoanModel;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.LoanService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final StaffRepository staffRepository;

    public LoanController(LoanService loanService,
                            LoanRepository loanRepository,
                            StaffRepository staffRepository,
                            UserRepository userRepository) {
        this.loanService = loanService;
        this.loanRepository = loanRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping
    public String listLoans(Model model) {
        List<LoanModel> loans = loanRepository.findAll();
        
        // Calculate totals
        double totalLoans = loans.stream().mapToDouble(LoanModel::getAmount).sum();
        double totalRepaid = loans.stream().mapToDouble(LoanModel::getAmountRepaid).sum();
        double totalOutstanding = totalLoans - totalRepaid;
        
        model.addAttribute("loan", new LoanModel());
        model.addAttribute("loans", loans);
        model.addAttribute("totalLoans", totalLoans);
        model.addAttribute("totalRepaid", totalRepaid);
        model.addAttribute("totalOutstanding", totalOutstanding);
        model.addAttribute("staffList", staffRepository.findAll());
        return "staff-loans/list";
    }

    @PostMapping
    public String saveLoan(@Valid @ModelAttribute("Loan") LoanDto Loan,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<LoanModel> loans = loanRepository.findAll();
            // Calculate totals
            double totalLoans = loans.stream().mapToDouble(LoanModel::getAmount).sum();
            double totalRepaid = loans.stream().mapToDouble(LoanModel::getAmountRepaid).sum();
            double totalOutstanding = totalLoans - totalRepaid;
            
            model.addAttribute("loans", loanRepository.findAll());
            model.addAttribute("staffList", staffRepository.findAll());
            model.addAttribute("totalLoans", totalLoans);
            model.addAttribute("totalRepaid", totalRepaid);
            model.addAttribute("totalOutstanding", totalOutstanding);
            return "staff-loans/list";
        }
        loanService.saveOrUpdateLoan(Loan);
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
                                @Valid @ModelAttribute("Loan") LoanDto loan,
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
}
