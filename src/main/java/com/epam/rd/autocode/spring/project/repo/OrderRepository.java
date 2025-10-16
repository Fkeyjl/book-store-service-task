package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user " +
           "JOIN FETCH o.bookItems oi " +
           "JOIN FETCH oi.book b " +
           "LEFT JOIN FETCH b.categories " +
           "WHERE o.user.id = :userId " +
           "ORDER BY o.orderDate DESC")
    List<Order> findOrdersWithAllDetailsByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.user " +
           "JOIN FETCH o.bookItems oi " +
           "JOIN FETCH oi.book b " +
           "LEFT JOIN FETCH b.categories " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAllDetails(@Param("orderId") Long orderId);
}
