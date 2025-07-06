package com.shuaibu.service.impl;

import com.shuaibu.dto.StaffDto;
import com.shuaibu.mapper.StaffMapper;
import com.shuaibu.model.StaffModel;
import com.shuaibu.repository.StaffRepository;
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
        StaffModel staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found with ID: " + id));
        return StaffMapper.mapToDto(staff);
    }

    @Override
    public void saveOrUpdateStaff(StaffDto staffDto) {
        if (staffDto == null) {
            throw new IllegalArgumentException("Staff data must not be null");
        }

        // Optional: Add logic to prevent duplicate usernames or emails
        staffRepository.save(StaffMapper.mapToModel(staffDto));
    }

    @Override
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot delete. Staff not found with ID: " + id);
        }
        staffRepository.deleteById(id);
    }
}
