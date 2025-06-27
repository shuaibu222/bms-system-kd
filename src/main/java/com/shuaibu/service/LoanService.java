package com.shuaibu.service;

import com.shuaibu.dto.LoanDto;
import java.util.List;

public interface LoanService {
    void saveOrUpdateLoan(LoanDto loanDto);
    void deleteLoan(Long id);
    LoanDto getLoanById(Long id);
    List<LoanDto> getAllLoans();
    List<LoanDto> getLoansByStaffId(Long staffId);
}
