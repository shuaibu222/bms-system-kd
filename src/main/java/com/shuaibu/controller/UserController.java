package com.shuaibu.controller;

import com.shuaibu.dto.UserDto;
import com.shuaibu.model.UserModel;
import com.shuaibu.service.UserService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    public String saveUser(@Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "users/list";
        }
        userService.saveOrUpdateUser(userDto);
        return "redirect:/users?success";
    }

    @GetMapping("/users/edit/{id}")
    public String updateUserForm(@PathVariable Long id, Model model) {
        UserDto userDto = userService.getUserById(id);
        model.addAttribute("user", userDto);
        return "users/edit";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "users/edit";
        }
        userDto.setId(id);
        userService.saveOrUpdateUser(userDto);
        return "redirect:/users?updateSuccess";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users?error";
    }
}
