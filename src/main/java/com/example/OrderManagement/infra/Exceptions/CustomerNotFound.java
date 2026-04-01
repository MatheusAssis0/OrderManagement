package com.example.OrderManagement.infra.Exceptions;

public class CustomerNotFound extends RuntimeException {
    public CustomerNotFound(String message) {
        super(message);
    }
}
