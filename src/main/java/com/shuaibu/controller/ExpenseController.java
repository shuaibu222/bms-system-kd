package com.shuaibu.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.ExpenseDto;
import com.shuaibu.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public String listExpenses(Model model) {
        List<ExpenseDto> expenses = expenseService.getAllExpenses();

        double totalExpenses = expenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();

        model.addAttribute("expenses", expenses);
        model.addAttribute("expense", new ExpenseDto()); // form binding
        model.addAttribute("totalExpenses", totalExpenses);

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
            BindingResult result) {
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
        return "redirect:/expenses?deleted";
    }
}
