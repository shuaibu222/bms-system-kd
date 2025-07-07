package com.shuaibu.service.impl;

import com.shuaibu.dto.LoanDto;
import com.shuaibu.mapper.LoanMapper;
import com.shuaibu.model.LoanModel;
import com.shuaibu.model.StaffModel;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.service.LoanService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final StaffRepository staffRepository;

    @Override
    public void saveOrUpdateLoan(LoanDto loanDto) {
        if (loanDto == null) {
            throw new IllegalArgumentException("Loan data must not be null");
        }

        // Default amountRepaid to 0 if null
        if (loanDto.getAmountRepaid() == null) {
            loanDto.setAmountRepaid(0.0);
        }

        // Ensure staff exists
        StaffModel staff = staffRepository.findById(loanDto.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Staff not found with ID: " + loanDto.getStaffId()));

        // Map and save loan
        LoanModel loanModel = LoanMapper.mapToModel(loanDto);
        loanModel.setStaffName(staff.getName());

        loanRepository.save(loanModel);
    }

    @Override
    public void deleteLoan(Long id) {
        if (!loanRepository.existsById(id)) {
            throw new IllegalArgumentException("Loan not found with ID: " + id);
        }
        loanRepository.deleteById(id);
    }

    @Override
    public LoanDto getLoanById(Long id) {
        LoanModel loan = loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + id));

        return LoanMapper.mapToDto(loan);
    }

    @Override
    public List<LoanDto> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(LoanMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> getLoansByStaffId(Long staffId) {
        return loanRepository.findByStaffId(staffId)
                .stream()
                .map(LoanMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
