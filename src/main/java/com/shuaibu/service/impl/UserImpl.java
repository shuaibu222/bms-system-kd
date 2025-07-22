package com.shuaibu.service.impl;

import com.shuaibu.dto.UserDto;
import com.shuaibu.mapper.UserMapper;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.shuaibu.mapper.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void createAdminUserIfNotExists() {
        String adminUsername = "Admin";
        String adminRole = "ROLE_ADMIN";

        List<UserModel> activeAdmins = userRepository.findManyByUsernameAndIsActive(adminUsername, "true");

        if (activeAdmins.size() > 1) {
            System.out.println("Warning: Multiple active admin users found. Manual cleanup may be needed.");
        } else if (activeAdmins.isEmpty()) {
            UserModel adminUser = new UserModel();
            adminUser.setFullName("Administrator");
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setIsActive("true");
            adminUser.setRole(adminRole);
            userRepository.save(adminUser);
        }
    }

    @Override
    public void createDeveloperUserIfNotExists() {
        String developerUsername = "Developer";
        String developerRole = "ROLE_DEVELOPER";

        List<UserModel> activeDevelopers = userRepository.findManyByUsernameAndIsActive(developerUsername, "true");

        if (activeDevelopers.size() > 1) {
            System.out.println("Warning: Multiple active developer users found. Manual cleanup may be needed.");
        } else if (activeDevelopers.isEmpty()) {
            UserModel developerUser = new UserModel();
            developerUser.setFullName("Shuaibu");
            developerUser.setUsername(developerUsername);
            developerUser.setPassword(passwordEncoder.encode("developer"));
            developerUser.setIsActive("true");
            developerUser.setRole(developerRole);
            userRepository.save(developerUser);
        }
    }

    @Override
    public void saveOrUpdateUser(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User data must not be null");
        }

        if (userDto.getId() == null) {
            // New user creation
            if (userRepository.findByUsername(userDto.getUsername()) != null) {
                throw new IllegalArgumentException("User with this username already exists");
            }
        } else {
            // Updating existing user
            boolean isDuplicate = userRepository.existsByUsernameAndIdNot(userDto.getUsername(), userDto.getId());
            if (isDuplicate) {
                throw new IllegalArgumentException("Username already taken by another user");
            }
        }

        UserModel user = mapToModel(userDto);
        user.setRole("ROLE_" + userDto.getRole().toUpperCase());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // always encode on save/update
        userRepository.save(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot delete. User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
