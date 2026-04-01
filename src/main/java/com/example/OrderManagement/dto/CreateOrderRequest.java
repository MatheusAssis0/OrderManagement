package com.example.OrderManagement.dto;

import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        List<OrderItemRequest> items
) {}
