package com.shuaibu.mapper;

import com.shuaibu.dto.UserDto;
import com.shuaibu.model.UserModel;

public class UserMapper {
    
    public static UserDto mapToDto(UserModel userModel){

        return UserDto.builder()
                .id(userModel.getId())
                .fullName(userModel.getFullName())
                .username(userModel.getUsername())
                .password(userModel.getPassword())
                .isActive(userModel.getIsActive())
                .role(userModel.getRole())
        .build();
    }

    public static UserModel mapToModel(UserDto userDto){

        return UserModel.builder()
                .id(userDto.getId())
                .fullName(userDto.getFullName())
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .isActive(userDto.getIsActive())
                .role(userDto.getRole())
        .build();
    }
}
