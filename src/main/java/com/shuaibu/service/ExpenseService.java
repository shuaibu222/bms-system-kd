package com.shuaibu.service;

import com.shuaibu.dto.ExpenseDto;
import java.util.List;

public interface ExpenseService {
    List<ExpenseDto> getAllExpenses();
    ExpenseDto getExpenseById(Long id);
    void saveOrUpdateExpense(ExpenseDto expenseDto);
    void deleteExpense(Long id);
}