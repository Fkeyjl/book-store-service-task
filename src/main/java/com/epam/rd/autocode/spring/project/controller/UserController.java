package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.UserService;
import com.epam.rd.autocode.spring.project.utils.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userForm", new UserRegistrationDTO());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") UserRegistrationDTO userDTO) {
        userService.register(userDTO);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        if (!model.containsAttribute("loginError")) {
            model.addAttribute("loginError", null);
        }
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                        HttpServletResponse response,
                        Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            Cookie cookie = new Cookie("JWT_TOKEN", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge((int) jwtExpirationInMs / 1000);
            response.addCookie(cookie);
            return "redirect:/";
        } catch (BadCredentialsException e) {
            model.addAttribute("loginError", "Invalid username or password");
            return "user/login";
        } catch (LockedException e) {
            model.addAttribute("loginError", "Your account has been locked. Please contact support.");
            return "user/login";
        }
    }

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
    public String editProfile(@ModelAttribute("userForm") UserUpdateDTO dto) {
        try {
            userService.updateUserProfile(dto);
        } catch (Exception e) {
            return  "redirect:/profile";
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
