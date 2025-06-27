package com.shuaibu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shuaibu.dto.UserDto;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.UserRepository;
import com.shuaibu.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "users/login";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserDto> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", new UserModel());
        return "users/list";
    }

    @PostMapping("/users")
    public String saveUser(@Valid @ModelAttribute("user") UserDto user,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "users/list";
        }
        userService.saveOrUpdateUser(user);
        return "redirect:/users?success";
    }

    @GetMapping("users/edit/{id}")
    public String updateUserForm(@PathVariable Long id, Model model) {
        UserDto user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "users/edit";
    }

    @PostMapping("users/update/{id}")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute("user") UserDto user,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "users/edit";
        }
        user.setId(id);
        userService.saveOrUpdateUser(user);
        return "redirect:/users?updateSuccess";
    }

    @GetMapping("users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users?error";
    }
}
