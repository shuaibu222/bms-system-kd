package com.shuaibu.service.impl;

import com.shuaibu.dto.ExpenseDto;
import com.shuaibu.mapper.ExpenseMapper;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.service.ExpenseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public List<ExpenseDto> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(ExpenseMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseDto getExpenseById(Long id) {
        return ExpenseMapper.mapToDto(
                expenseRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Expense not found!"))
        );
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