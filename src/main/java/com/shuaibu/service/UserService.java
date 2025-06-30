package com.shuaibu.service;

import java.util.List;

import com.shuaibu.dto.UserDto;

public interface UserService {
    List<UserDto> getAllUsers();

    void createAdminUserIfNotExists();

    UserDto getUserById(Long id);

    void deleteUser(Long id);

    void saveOrUpdateUser(UserDto user);
}
