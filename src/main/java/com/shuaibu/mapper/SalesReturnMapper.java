package com.shuaibu.mapper;

import com.shuaibu.dto.SalesReturnDto;
import com.shuaibu.model.SalesReturnModel;

public class SalesReturnMapper {

    public static SalesReturnDto mapToDto(SalesReturnModel salesReturnModel) {
        return SalesReturnDto.builder()
                .id(salesReturnModel.getId())
                .invNum(salesReturnModel.getInvNum())
                .reason(salesReturnModel.getReason())
                .date(salesReturnModel.getDate())
                .build();
    }

    public static SalesReturnModel mapToModel(SalesReturnDto salesReturnDto) {
        return SalesReturnModel.builder()
                .id(salesReturnDto.getId())
                .invNum(salesReturnDto.getInvNum())
                .reason(salesReturnDto.getReason())
                .date(salesReturnDto.getDate())
                .build();
    }
}