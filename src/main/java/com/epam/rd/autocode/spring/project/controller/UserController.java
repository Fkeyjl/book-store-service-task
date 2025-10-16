package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CustomerDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.UserUpdateDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal Jwt jwt, Model model) {
        String id = jwt.getClaimAsString("jti");
        CustomerDTO user = userService.findUserById(Long.valueOf(id));
        List<OrderDTO> orders = orderService.getOrdersByClientId(Long.valueOf(id));
        UserUpdateDTO userForm = UserUpdateDTO.fromCustomerDTO(user);
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("userForm", userForm);
        return "user/profile";
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String editProfile(@ModelAttribute("userForm") UserUpdateDTO dto) {
        try {
            userService.updateUserProfile(dto);
        } catch (Exception e) {
            return "redirect:/profile";
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteAccount(@AuthenticationPrincipal Jwt jwtPrincipal) {
        Long userId = Long.valueOf(jwtPrincipal.getClaimAsString("jti"));
        userService.deleteUser(userId);

        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
