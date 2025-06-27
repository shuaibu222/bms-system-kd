package com.shuaibu.controller;

import com.shuaibu.dto.StaffDto;
import com.shuaibu.model.StaffModel;
import com.shuaibu.repository.StaffRepository;
import com.shuaibu.service.StaffService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staffs")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService, StaffRepository staffRepository) {
        this.staffService = staffService;
    }

    @GetMapping
    public String showStaffList(Model model) {
        model.addAttribute("staffs", staffService.getAllStaffs());
        model.addAttribute("staff", new StaffModel());
        return "staffs/list";
    }

    @PostMapping
    public String saveStaff(@Valid @ModelAttribute("staff") StaffDto staffDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("staffs", staffService.getAllStaffs());
            return "staffs/list";
        }
        staffService.saveOrUpdateStaff(staffDto);
        return "redirect:/staffs?success";
    }

    @GetMapping("/edit/{id}")
    public String editStaffForm(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.getStaffById(id));
        return "staffs/edit";
    }

    @PostMapping("/update/{id}")
    public String updateStaff(@PathVariable Long id, @Valid @ModelAttribute("staff") StaffDto staffDto, BindingResult result) {
        if (result.hasErrors()) {
            return "staffs/edit";
        }
        staffDto.setId(id);
        staffService.saveOrUpdateStaff(staffDto);
        return "redirect:/staffs?updateSuccess";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return "redirect:/staffs?error";
    }

}