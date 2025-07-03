package com.shuaibu.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.ExpenseDto;
import com.shuaibu.model.ExpenseModel;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.service.ExpenseService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    public ExpenseController(ExpenseService expenseService, ExpenseRepository expenseRepository) {
        this.expenseService = expenseService;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public String listExpenses(Model model) {
        List<ExpenseModel> expenses = expenseRepository.findAll();

        // Calculate total
        double totalExpenses = expenses.stream()
                .map(ExpenseModel::getAmount)
                .mapToDouble(amount -> amount != null ? amount.doubleValue() : 0.0)
                .sum();

        model.addAttribute("expenses", expenses);
        model.addAttribute("expense", new ExpenseModel());
        model.addAttribute("totalExpenses", totalExpenses); // Add total to model

        return "expenses/list";
    }

    @PostMapping
    public String saveExpense(@Valid @ModelAttribute("expense") ExpenseDto expense,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("expenses", expenseService.getAllExpenses());
            return "expenses/list";
        }
        expenseService.saveOrUpdateExpense(expense);
        return "redirect:/expenses?success";
    }

    @GetMapping("/edit/{id}")
    public String updateExpenseForm(@PathVariable Long id, Model model) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        model.addAttribute("expense", expense);
        return "expenses/edit";
    }

    @PostMapping("/update/{id}")
    public String updateExpense(@PathVariable Long id,
            @Valid @ModelAttribute("expense") ExpenseDto expense,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "expenses/edit";
        }
        expense.setId(id);
        expenseService.saveOrUpdateExpense(expense);
        return "redirect:/expenses?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/expenses?error";
    }
}