package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.UserUpdateDTO;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    @GetMapping("/employees")
    public String showAllEmployees(Model model) {
        List<EmployeeDTO> employees = userService.findAllEmployees();
        model.addAttribute("employees", employees);
        return "admin/manage-employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String showEditEmployeeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            EmployeeDTO employee = userService.findEmployeeById(id);
            model.addAttribute("employee", employee);
            return "admin/edit-employee";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Робітника не знайдено");
            return "redirect:/admin/employees";
        }
    }

    @PostMapping("/employees/{id}/edit")
    public String updateEmployee(@PathVariable Long id,
                                 @ModelAttribute UserUpdateDTO userUpdateDTO,
                                 RedirectAttributes redirectAttributes) {
        try {
            userUpdateDTO.setId(id);
            userService.updateUserProfile(userUpdateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Дані робітника успішно оновлено");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Робітника не знайдено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка оновлення даних: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Робітника успішно видалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення робітника: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{id}/block")
    public String blockEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.blockUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Робітника заблоковано");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка блокування: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{id}/unblock")
    public String unblockEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unblockUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Робітника розблоковано");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка розблокування: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }
}
