package com.example.OrderManagement.repository;

import com.example.OrderManagement.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findById(Long id);
}
