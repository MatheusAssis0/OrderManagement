package com.example.OrderManagement.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        @NotEmpty List<OrderItemRequest> items
) {}
