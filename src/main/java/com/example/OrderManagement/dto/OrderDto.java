package com.example.OrderManagement.dto;

import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.models.Order;
import com.example.OrderManagement.models.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(Long id, Customer customer,@NotBlank LocalDateTime createdAt,@NotBlank Status status,
                      @NotBlank @Positive BigDecimal totalAmount) {
    public OrderDto(Order order){
        this(order.getId(), order.getCustomer(), order.getCreatedAt(), order.getStatus(), order.getTotalAmount());
    }
}
