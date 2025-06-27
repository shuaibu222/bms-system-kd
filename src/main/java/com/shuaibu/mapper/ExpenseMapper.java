package com.shuaibu.mapper;

import com.shuaibu.dto.ExpenseDto;
import com.shuaibu.model.ExpenseModel;

public class ExpenseMapper {

    public static ExpenseDto mapToDto(ExpenseModel expenseModel) {
        return ExpenseDto.builder()
                .id(expenseModel.getId())
                .description(expenseModel.getDescription())
                .amount(expenseModel.getAmount())
                .date(expenseModel.getDate())
                .build();
    }

    public static ExpenseModel mapToModel(ExpenseDto expenseDto) {
        return ExpenseModel.builder()
                .id(expenseDto.getId())
                .description(expenseDto.getDescription())
                .amount(expenseDto.getAmount())
                .date(expenseDto.getDate())
                .build();
    }
}