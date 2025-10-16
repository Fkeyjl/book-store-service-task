package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.Cart;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Status;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByClientId(Long id) {
        return orderRepository.findOrdersWithAllDetailsByUserId(id).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithAllDetails(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
        return modelMapper.map(order, OrderDTO.class);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        Status oldStatus = order.getStatus();

        if (order.getStatus() == Status.DELIVERED && newStatus == Status.PENDING) {
            throw new IllegalStateException("Unable to change status from DELIVERED to PENDING");
        }

        // Повернення коштів при зміні статусу на CANCELLED
        if (newStatus == Status.CANCELLED && oldStatus != Status.CANCELLED) {
            userService.addUserBalance(order.getUser().getId(), order.getPrice());
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return List.of();
    }

    @Override
    public OrderDTO addOrder(OrderDTO order) {
        return null;
    }

    @Override
    @Transactional
    public void createOrderFromCart(Cart cart, Long userId) {
        User user = userService.updateUserBalance(userId, cart.getTotalPrice());
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.PENDING);
        order.setPrice(cart.getTotalPrice());

        List<BookItem> bookItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Book book = bookRepository.findById(entry.getKey()).orElseThrow(EntityNotFoundException::new);

            BookItem item = new BookItem();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(entry.getValue());
            item.setPricePerUnit(book.getPrice());

            bookItems.add(item);
        }

        order.setBookItems(bookItems);
        orderRepository.save(order);
    }
}
