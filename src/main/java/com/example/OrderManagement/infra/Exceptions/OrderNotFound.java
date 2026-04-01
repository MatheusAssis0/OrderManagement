package com.example.OrderManagement.infra.Exceptions;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(String message) {
        super(message);
    }
}
