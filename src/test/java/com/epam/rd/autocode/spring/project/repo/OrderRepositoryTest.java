package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.model.enums.Status;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Order testOrder1;
    private Order testOrder2;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.CUSTOMER);
        testUser.setBalance(new BigDecimal("500.00"));
        testUser.setIsBlocked(false);
        entityManager.persist(testUser);

        testBook = new Book();
        testBook.setName("Test Book");
        testBook.setIsbn("978-0-123456-47-2");
        testBook.setAuthor("Test Author");
        testBook.setPrice(new BigDecimal("29.99"));
        testBook.setPublicationDate(LocalDate.now());
        testBook.setPages(250);
        testBook.setLanguage(Language.ENGLISH);
        testBook.setAgeGroup(AgeGroup.ADULT);
        entityManager.persist(testBook);

        testOrder1 = new Order();
        testOrder1.setUser(testUser);
        testOrder1.setOrderDate(LocalDateTime.now().minusDays(2));
        testOrder1.setStatus(Status.PENDING);
        testOrder1.setPrice(new BigDecimal("29.99"));
        entityManager.persist(testOrder1);

        BookItem bookItem1 = new BookItem();
        bookItem1.setBook(testBook);
        bookItem1.setOrder(testOrder1);
        bookItem1.setQuantity(1);
        bookItem1.setPricePerUnit(new BigDecimal("29.99"));
        entityManager.persist(bookItem1);

        testOrder2 = new Order();
        testOrder2.setUser(testUser);
        testOrder2.setOrderDate(LocalDateTime.now().minusDays(1));
        testOrder2.setStatus(Status.DELIVERED);
        testOrder2.setPrice(new BigDecimal("59.98"));
        entityManager.persist(testOrder2);

        BookItem bookItem2 = new BookItem();
        bookItem2.setBook(testBook);
        bookItem2.setOrder(testOrder2);
        bookItem2.setQuantity(2);
        bookItem2.setPricePerUnit(new BigDecimal("29.99"));
        entityManager.persist(bookItem2);

        entityManager.flush();
    }

    @Test
    void testFindOrdersWithAllDetailsByUserId_ShouldReturnOrdersWithBookItems() {
        List<Order> orders = orderRepository.findOrdersWithAllDetailsByUserId(testUser.getId());

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrderDate()).isAfter(orders.get(1).getOrderDate());
        assertThat(orders).extracting(Order::getStatus)
                .containsExactly(Status.DELIVERED, Status.PENDING);
    }

    @Test
    void testFindOrdersWithAllDetailsByUserId_ShouldLoadBookItems() {
        entityManager.flush();
        entityManager.clear();
        
        List<Order> orders = orderRepository.findOrdersWithAllDetailsByUserId(testUser.getId());

        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getBookItems()).isNotEmpty();
        assertThat(orders.get(0).getBookItems().get(0).getBook()).isNotNull();
    }

    @Test
    void testFindOrdersWithAllDetailsByUserId_WhenNoOrders_ShouldReturnEmptyList() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password");
        newUser.setRole(Role.CUSTOMER);
        newUser.setIsBlocked(false);
        entityManager.persist(newUser);
        entityManager.flush();

        List<Order> orders = orderRepository.findOrdersWithAllDetailsByUserId(newUser.getId());

        assertThat(orders).isEmpty();
    }

    @Test
    void testFindAll_ShouldReturnAllOrders() {
        List<Order> orders = orderRepository.findAll();

        assertThat(orders).hasSize(2);
    }

    @Test
    void testFindById_WhenOrderExists_ShouldReturnOrder() {
        Optional<Order> found = orderRepository.findById(testOrder1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Status.PENDING);
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
    }

    @Test
    void testFindById_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        Optional<Order> found = orderRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void testSaveOrder_ShouldPersistOrder() {
        Order newOrder = new Order();
        newOrder.setUser(testUser);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(Status.PENDING);
        newOrder.setPrice(new BigDecimal("49.99"));

        Order saved = orderRepository.save(newOrder);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void testUpdateOrder_ShouldUpdateOrderStatus() {
        Order order = orderRepository.findById(testOrder1.getId()).orElseThrow();
        order.setStatus(Status.DELIVERED);

        Order updated = orderRepository.save(order);

        assertThat(updated.getStatus()).isEqualTo(Status.DELIVERED);
    }

    @Test
    void testDeleteOrder_ShouldRemoveOrder() {
        Long orderId = testOrder1.getId();
        
        entityManager.detach(testOrder2);
        orderRepository.deleteById(orderId);

        Optional<Order> deleted = orderRepository.findById(orderId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testOrderWithMultipleBookItems() {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.PENDING);
        order.setPrice(new BigDecimal("89.97"));
        entityManager.persist(order);

        Book book2 = new Book();
        book2.setName("Another Book");
        book2.setIsbn("978-0-123456-48-9");
        book2.setAuthor("Another Author");
        book2.setPrice(new BigDecimal("19.99"));
        book2.setPublicationDate(LocalDate.now());
        book2.setPages(200);
        book2.setLanguage(Language.ENGLISH);
        book2.setAgeGroup(AgeGroup.ADULT);
        entityManager.persist(book2);

        BookItem item1 = new BookItem();
        item1.setBook(testBook);
        item1.setOrder(order);
        item1.setQuantity(1);
        item1.setPricePerUnit(new BigDecimal("29.99"));
        entityManager.persist(item1);

        BookItem item2 = new BookItem();
        item2.setBook(book2);
        item2.setOrder(order);
        item2.setQuantity(3);
        item2.setPricePerUnit(new BigDecimal("19.99"));
        entityManager.persist(item2);

        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(foundOrder.getBookItems()).hasSize(2);
    }

    @Test
    void testOrdersSortedByDateDescending() {
        List<Order> orders = orderRepository.findOrdersWithAllDetailsByUserId(testUser.getId());

        assertThat(orders).hasSize(2);
        LocalDateTime firstOrderDate = orders.get(0).getOrderDate();
        LocalDateTime secondOrderDate = orders.get(1).getOrderDate();
        assertThat(firstOrderDate).isAfter(secondOrderDate);
    }
}
