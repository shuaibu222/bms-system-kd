package com.shuaibu.service;

import com.shuaibu.dto.StaffDto;

import java.util.List;

public interface StaffService {
    List<StaffDto> getAllStaffs();

    StaffDto getStaffById(Long id);

    void saveOrUpdateStaff(StaffDto staffDto);

    void deleteStaff(Long id);
}