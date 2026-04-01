package com.example.OrderManagement.infra.Exceptions;

public class OrderStatus extends RuntimeException {
    public OrderStatus(String message) {
        super(message);
    }
}
