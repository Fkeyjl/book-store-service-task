package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderStatusUpdateDto;
import com.epam.rd.autocode.spring.project.model.enums.Status;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String getOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("allStatuses", Status.values());
        model.addAttribute("statusUpdateFrom", new OrderStatusUpdateDto());
        return "management/orders";
    }

    @PostMapping("/orders/update-status")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String updateStatus(@ModelAttribute OrderStatusUpdateDto dto,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(dto.getOrderId(), dto.getNewStatus());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус замовлення #" + dto.getOrderId() + " оновлено на " + dto.getNewStatus());
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка оновлення статусу.");
        }

        return "redirect:/orders";
    }
}
