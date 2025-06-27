package com.shuaibu.mapper;

import com.shuaibu.dto.LoanDto;
import com.shuaibu.model.LoanModel;

public class LoanMapper {

    public static LoanDto mapToDto(LoanModel model) {
        return LoanDto.builder()
                .id(model.getId())
                .staffId(model.getStaffId())
                .amount(model.getAmount())
                .purpose(model.getPurpose())
                .loanDate(model.getLoanDate())
                .amountRepaid(model.getAmountRepaid())
                .build();
    }

    public static LoanModel mapToModel(LoanDto dto) {
        return LoanModel.builder()
                .id(dto.getId())
                .staffId(dto.getStaffId())
                .amount(dto.getAmount())
                .purpose(dto.getPurpose())
                .loanDate(dto.getLoanDate())
                .amountRepaid(dto.getAmountRepaid())
                .build();
    }
}
