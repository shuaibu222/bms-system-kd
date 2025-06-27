package com.shuaibu.mapper;

import com.shuaibu.dto.MainLedgerDto;
import com.shuaibu.model.MainLedgerModel;

public class MainLedgerMapper {
    public static MainLedgerDto mapToDto(MainLedgerModel mainLedgerModel) {
        return MainLedgerDto.builder()
                .id(mainLedgerModel.getId())
                .date(mainLedgerModel.getDate())
                .closingBalance(mainLedgerModel.getClosingBalance())
                .openingBalance(mainLedgerModel.getOpeningBalance())
                .particulars(mainLedgerModel.getParticulars())
                .credit(mainLedgerModel.getCredit())
                .debit(mainLedgerModel.getDebit())
                .build();
    }

    public static MainLedgerModel mapToModel(MainLedgerDto mainLedgerDto) {
        return MainLedgerModel.builder()
                .id(mainLedgerDto.getId())
                .date(mainLedgerDto.getDate())
                .debit(mainLedgerDto.getDebit())
                .credit(mainLedgerDto.getCredit())
                .particulars(mainLedgerDto.getParticulars())
                .closingBalance(mainLedgerDto.getClosingBalance())
                .openingBalance(mainLedgerDto.getOpeningBalance())
                .build();
    }
}
