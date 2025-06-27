package com.shuaibu.mapper;

import com.shuaibu.dto.SuspenseDto;
import com.shuaibu.model.SuspenseModel;

public class SuspenseMapper {

    public static SuspenseDto mapToDto(SuspenseModel suspenseModel) {
        return SuspenseDto.builder()
                .id(suspenseModel.getId())
                .amount(suspenseModel.getAmount())
                .paymentMethod(suspenseModel.getPaymentMethod())
                .transactionDate(suspenseModel.getTransactionDate())
                .reason(suspenseModel.getReason())
                .referenceId(suspenseModel.getReferenceId())
                .status(suspenseModel.getStatus())
                .build();
    }

    public static SuspenseModel mapToModel(SuspenseDto suspenseDto) {        

        return SuspenseModel.builder()
                .id(suspenseDto.getId())
                .amount(suspenseDto.getAmount())
                .paymentMethod(suspenseDto.getPaymentMethod())
                .transactionDate(suspenseDto.getTransactionDate())
                .reason(suspenseDto.getReason())
                .referenceId(suspenseDto.getReferenceId())
                .status(suspenseDto.getStatus())
                .build();
    }
}
