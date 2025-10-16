package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testCustomer;
    private User testEmployee;

    @BeforeEach
    void setUp() {
        testCustomer = new User();
        testCustomer.setName("John Customer");
        testCustomer.setEmail("customer@example.com");
        testCustomer.setPassword("password123");
        testCustomer.setPhone("+380501234567");
        testCustomer.setBirthDate(LocalDate.of(1990, 5, 15));
        testCustomer.setBalance(new BigDecimal("100.00"));
        testCustomer.setRole(Role.CUSTOMER);
        testCustomer.setIsBlocked(false);
        entityManager.persist(testCustomer);

        testEmployee = new User();
        testEmployee.setName("Jane Employee");
        testEmployee.setEmail("employee@example.com");
        testEmployee.setPassword("password456");
        testEmployee.setIsBlocked(false);
        testEmployee.setRole(Role.EMPLOYEE);
        entityManager.persist(testEmployee);

        entityManager.flush();
    }

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("customer@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Customer");
        assertThat(found.get().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("customer@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(23);
    }

    @Test
    void testSaveUser_ShouldPersistUser() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("newpass");
        newUser.setRole(Role.CUSTOMER);
        newUser.setBalance(new BigDecimal("50.00"));
        newUser.setIsBlocked(false);

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("newuser@example.com");
        assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void testUpdateUser_ShouldUpdateUserDetails() {
        User user = userRepository.findByEmail("customer@example.com").orElseThrow();
        user.setName("Updated Name");
        user.setBalance(new BigDecimal("200.00"));

        User updated = userRepository.save(user);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void testDeleteUser_ShouldRemoveUser() {
        User user = userRepository.findByEmail("customer@example.com").orElseThrow();
        Long userId = user.getId();

        userRepository.delete(user);
        entityManager.flush();

        Optional<User> deleted = userRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testFindByRole_Customer_ShouldReturnCustomers() {
        List<User> customers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .toList();

        assertThat(customers).hasSize(11);
        assertThat(customers.get(10).getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void testFindByRole_Employee_ShouldReturnEmployees() {
        List<User> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        assertThat(employees).hasSize(11);
        assertThat(employees.get(10).getEmail()).isEqualTo("employee@example.com");
    }
}
