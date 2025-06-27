package com.shuaibu.service.impl;

import com.shuaibu.dto.StaffDto;
import com.shuaibu.dto.DepositDto;
import com.shuaibu.mapper.StaffMapper;
import com.shuaibu.mapper.DepositMapper;
import com.shuaibu.model.StaffModel;
import com.shuaibu.model.DepositModel;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.repository.DepositRepository;
import com.shuaibu.service.StaffService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffImpl implements StaffService {

    private final StaffRepository staffRepository;

    public StaffImpl(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public List<StaffDto> getAllStaffs() {
        return staffRepository.findAll()
                .stream()
                .map(StaffMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public StaffDto getStaffById(Long id) {
        return StaffMapper.mapToDto(
                staffRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Staff not found!"))
        );
    }

    @Override
    public void saveOrUpdateStaff(StaffDto staffDto) {
        staffRepository.save(StaffMapper.mapToModel(staffDto));
    }

    @Override
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }
}