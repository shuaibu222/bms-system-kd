package com.shuaibu.service.impl;

import com.shuaibu.dto.ExpenseDto;
import com.shuaibu.mapper.ExpenseMapper;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.service.ExpenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public List<ExpenseDto> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(ExpenseMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseDto getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .map(ExpenseMapper::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found!"));
    }

    @Override
    public void saveOrUpdateExpense(ExpenseDto expenseDto) {
        expenseRepository.save(ExpenseMapper.mapToModel(expenseDto));
    }

    @Override
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
}
