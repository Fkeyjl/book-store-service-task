package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.model.enums.Status;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(Role.CUSTOMER);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(Status.PENDING);
        testOrder.setPrice(BigDecimal.valueOf(100.00));

        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setStatus(Status.PENDING);
        testOrderDTO.setPrice(BigDecimal.valueOf(100.00));
    }

    @Test
    void testGetOrderById_WhenOrderExists_ShouldReturnOrderDTO() {
        when(orderRepository.findByIdWithAllDetails(1L)).thenReturn(Optional.of(testOrder));
        when(modelMapper.map(testOrder, OrderDTO.class)).thenReturn(testOrderDTO);

        OrderDTO result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        verify(orderRepository, times(1)).findByIdWithAllDetails(1L);
        verify(modelMapper, times(1)).map(testOrder, OrderDTO.class);
    }

    @Test
    void testGetOrderById_WhenOrderDoesNotExist_ShouldThrowException() {
        when(orderRepository.findByIdWithAllDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository, times(1)).findByIdWithAllDetails(999L);
    }

    @Test
    void testGetOrdersByClientId_ShouldReturnListOfOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(Status.PENDING);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(Status.DELIVERED);

        when(orderRepository.findOrdersWithAllDetailsByUserId(1L)).thenReturn(Arrays.asList(order1, order2));
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        List<OrderDTO> result = orderService.getOrdersByClientId(1L);

        assertThat(result).hasSize(2);
        verify(orderRepository, times(1)).findOrdersWithAllDetailsByUserId(1L);
        verify(modelMapper, times(2)).map(any(Order.class), eq(OrderDTO.class));
    }

    @Test
    void testUpdateOrderStatus_ShouldUpdateStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.updateOrderStatus(1L, Status.DELIVERED);

        assertThat(testOrder.getStatus()).isEqualTo(Status.DELIVERED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void testGetAllOrders_ShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(testOrder));
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(any(Order.class), eq(OrderDTO.class));
    }
}
