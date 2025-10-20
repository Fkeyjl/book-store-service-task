package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.LoginRequest;
import com.epam.rd.autocode.spring.project.dto.UserRegistrationDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.AuthenticationService;
import com.epam.rd.autocode.spring.project.service.UserService;
import com.epam.rd.autocode.spring.project.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuthenticationService authenticationService;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpirationInMs;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("signupError")) {
            model.addAttribute("signupError", null);
        }
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserRegistrationDTO());
        }
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userForm") UserRegistrationDTO userDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userForm", userDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", bindingResult);
            return "redirect:/register";
        }
        try {
            userService.register(userDTO);
            return "redirect:/login";
        } catch (AlreadyExistException e) {
            redirectAttributes.addFlashAttribute("signupError", e.getMessage());
            redirectAttributes.addFlashAttribute("userForm", userDTO);
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        if (!model.containsAttribute("loginError")) {
            model.addAttribute("loginError", null);
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                        RedirectAttributes redirectAttributes,
                        HttpServletResponse response) {
        try {
            if (authenticationService.isUserLocked(loginRequest.getUsername())) {
                redirectAttributes.addFlashAttribute("loginError", "Account is temporarily locked. Try again later.");
                redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
                return "redirect:/login";
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            authenticationService.resetFailedLoginAttempts(loginRequest.getUsername());

            String jwt = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            setCookies(response, jwt, refreshToken);
            return "redirect:/";
            
        } catch (BadCredentialsException e) {
            try {
                authenticationService.increaseFailedLoginAttempts(loginRequest.getUsername());
                redirectAttributes.addFlashAttribute("loginError", "Invalid username or password");
            } catch (LockedException lockEx) {
                redirectAttributes.addFlashAttribute("loginError", "Too many failed login attempts. Account is temporarily locked.");
            } catch (EntityNotFoundException notFound) {
                redirectAttributes.addFlashAttribute("loginError", "Invalid username or password");
            }
            redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
            return "redirect:/login";
            
        } catch (LockedException e) {
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
            return "redirect:/login";
        }
    }

    @PostMapping("/api/auth/refresh")
    public String refreshToken(HttpServletRequest request, HttpServletResponse response, 
                               RedirectAttributes redirectAttributes) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();

                    if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                        log.warn("Invalid or expired refresh token");
                        redirectAttributes.addFlashAttribute("loginError", "Session expired. Please login again.");
                        return "redirect:/login";
                    }

                    try {
                        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (!userDetails.isAccountNonLocked() || !userDetails.isEnabled()) {
                            log.warn("User account is locked or disabled: {}", username);
                            redirectAttributes.addFlashAttribute("loginError", "Account is locked or disabled.");
                            return "redirect:/login";
                        }

                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        String newAccessToken = jwtTokenProvider.generateToken(authentication);
                        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

                        setCookies(response, newAccessToken, newRefreshToken);

                        log.info("Tokens refreshed successfully for user: {}", username);
                        return "redirect:/";
                        
                    } catch (IllegalArgumentException e) {
                        log.error("Error extracting username from token: {}", e.getMessage());
                        redirectAttributes.addFlashAttribute("loginError", "Invalid token. Please login again.");
                        return "redirect:/login";
                    }
                }
            }
        }
        
        log.warn("No refresh token found in cookies");
        redirectAttributes.addFlashAttribute("loginError", "Session expired. Please login again.");
        return "redirect:/login";
    }

    private void setCookies(HttpServletResponse response, String jwt, String refreshToken) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) jwtExpirationInMs / 1000);
        response.addCookie(jwtCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) refreshTokenExpirationInMs / 1000);
        response.addCookie(refreshCookie);
    }
}
