package com.shuaibu.mapper;

import com.shuaibu.dto.StaffDto;
import com.shuaibu.model.StaffModel;

public class StaffMapper {

    public static StaffDto mapToDto(StaffModel staffModel) {
        return StaffDto.builder()
                .id(staffModel.getId())
                .name(staffModel.getName())
                .phone(staffModel.getPhone())
                .address(staffModel.getAddress())
                .build();
    }

    public static StaffModel mapToModel(StaffDto staffDto) {
        return StaffModel.builder()
                .id(staffDto.getId())
                .name(staffDto.getName())
                .phone(staffDto.getPhone())
                .address(staffDto.getAddress())
                .build();
    }
}