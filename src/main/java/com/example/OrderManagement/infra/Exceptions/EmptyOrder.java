package com.example.OrderManagement.infra.Exceptions;

public class EmptyOrder extends RuntimeException {
    public EmptyOrder(String message) {
        super(message);
    }
}
