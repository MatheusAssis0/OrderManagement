package com.example.OrderManagement.repository;

import com.example.OrderManagement.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
