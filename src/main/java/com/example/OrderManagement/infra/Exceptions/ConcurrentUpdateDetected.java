package com.example.OrderManagement.infra.Exceptions;

public class ConcurrentUpdateDetected extends RuntimeException {
    public ConcurrentUpdateDetected(String message) {
        super(message);
    }
}
