package com.example.OrderManagement.infra.Exceptions;

public class InsufficientStock extends RuntimeException {
    public InsufficientStock(String message) {
        super(message);
    }
}
