package com.shuaibu.service.impl;

import com.shuaibu.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.UserDto;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.UserService;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.UserMapper.*;

@Service
public class UserImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void createAdminUserIfNotExists() {
        String adminUsername = "Admin";
        String adminRole = "ROLE_ADMIN";
        
        // Check if an active admin user with the specific username already exists
        List<UserModel> activeAdmins = userRepository.findManyByUsernameAndIsActive(adminUsername, "true");
        
        if (activeAdmins.size() > 1) {
            // Log a warning instead of throwing an exception
            System.out.println("Warning: Multiple active admin users found. This should be checked.");
        } else if (activeAdmins.isEmpty()) {
            // No active admin found with this username, create a new admin user
            UserModel adminUser = new UserModel();
            adminUser.setFullName("Shuaibu");
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setIsActive("true");
            adminUser.setRole(adminRole);
            userRepository.save(adminUser);
        }
        // If there is exactly one active admin user, do nothing
    }

    @Override
    public void saveOrUpdateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            // Check for duplicates for new entry
            UserModel existingUser = userRepository.findByUsername(userDto.getUsername());
            if (existingUser != null) {
                throw new IllegalArgumentException("User with this username already exists");
            }
        } else {
            // Check for duplicates for existing entry
            boolean isDuplicate = userRepository.existsByUsernameAndIdNot(
                userDto.getUsername(),
                userDto.getId()
            );
            if (isDuplicate) {
                throw new IllegalArgumentException("Duplicate username found!");
            }
        }
        
        UserModel user = UserMapper.mapToModel(userDto);
        user.setRole("ROLE_" + userDto.getRole());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(UserMapper::mapToDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return mapToDto(userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found!")));
    }
    
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
