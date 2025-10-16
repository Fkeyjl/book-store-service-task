package com.epam.rd.autocode.spring.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == 401) {
                return "redirect:/login";
            }

            model.addAttribute("status", statusCode);
            model.addAttribute("error", getErrorTitle(statusCode));
            model.addAttribute("message", getErrorMessage(statusCode));
        } else {
            model.addAttribute("status", 500);
            model.addAttribute("error", "Internal Server Error");
            model.addAttribute("message", "An unexpected error occurred.");
        }

        return "error/error";
    }

    private String getErrorTitle(int statusCode) {
        return switch (statusCode) {
            case 403 -> "Access Denied";
            case 404 -> "Page Not Found";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }

    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 403 -> "You don't have permission to access this resource.";
            case 404 -> "The page you are looking for doesn't exist or has been moved.";
            case 500 -> "Something went wrong on our end. Please try again later.";
            default -> "An error occurred while processing your request.";
        };
    }
}
