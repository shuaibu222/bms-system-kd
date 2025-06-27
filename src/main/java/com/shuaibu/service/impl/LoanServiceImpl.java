package com.shuaibu.service.impl;

import com.shuaibu.dto.LoanDto;
import com.shuaibu.mapper.LoanMapper;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.model.LoanModel;
import com.shuaibu.model.StaffModel;
import com.shuaibu.service.LoanService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final StaffRepository staffRepository;

    public LoanServiceImpl(LoanRepository loanRepository, StaffRepository staffRepository) {
        this.loanRepository = loanRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public void saveOrUpdateLoan(LoanDto loanDto) {
        if (loanDto.getAmountRepaid() == null) {
            loanDto.setAmountRepaid(0.0);
        }

        StaffModel staff = staffRepository.findById(loanDto.getStaffId()).get();
        if (staff == null) {
            throw new IllegalArgumentException("Staff not found!");
        }

        LoanModel loanModel = LoanMapper.mapToModel(loanDto);
        loanModel.setStaffName(staff.getName());

        loanRepository.save(loanModel);
    }

    @Override
    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    @Override
    public LoanDto getLoanById(Long id) {
        return LoanMapper.mapToDto(loanRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Loan not found")));
    }

    @Override
    public List<LoanDto> getAllLoans() {
        return loanRepository.findAll().stream().map(LoanMapper::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> getLoansByStaffId(Long staffId) {
        return loanRepository.findByStaffId(staffId).stream().map(LoanMapper::mapToDto).collect(Collectors.toList());
    }
}
