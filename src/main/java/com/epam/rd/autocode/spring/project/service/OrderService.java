package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.Cart;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.Status;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByClientId(Long id);

    void updateOrderStatus(Long orderId, Status newStatus);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    void createOrderFromCart(Cart cart, Long userId);
}
