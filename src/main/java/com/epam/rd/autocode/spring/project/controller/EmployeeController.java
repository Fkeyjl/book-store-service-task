package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CustomerDTO;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/management")
public class EmployeeController {
    private final UserService userService;

    public EmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String showAllClients(Model model) {
        List<CustomerDTO> customers = userService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "management/manage-users";
    }

    @PostMapping("/users/{id}/block")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String blockClient(@PathVariable Long id) {
        userService.blockUser(id);
        return "redirect:/management/users";
    }

    @PostMapping("/users/{id}/unblock")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String unblockClient(@PathVariable Long id) {
        userService.unblockUser(id);
        return "redirect:/management/users";
    }
}
