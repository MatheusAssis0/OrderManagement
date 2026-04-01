package com.example.OrderManagement.infra.Exceptions;

public class ProductNotFound extends RuntimeException {
    public ProductNotFound(String message) {
        super(message);
    }
}
