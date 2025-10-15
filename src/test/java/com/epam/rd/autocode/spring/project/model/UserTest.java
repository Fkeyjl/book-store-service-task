package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setPhone("+380501234567");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setBalance(new BigDecimal("100.00"));
        user.setRole(Role.CUSTOMER);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getPhone()).isEqualTo("+380501234567");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(user.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void testUserEquality() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("john@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("john@example.com");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testUserDefaultValues() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getBalance()).isNull();
        assertThat(user.getRole()).isNull();
    }

    @Test
    void testUserWithDifferentRoles() {
        User customer = new User();
        customer.setRole(Role.CUSTOMER);

        User employee = new User();
        employee.setRole(Role.EMPLOYEE);

        assertThat(customer.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(employee.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(customer.getRole()).isNotEqualTo(employee.getRole());
    }
}
