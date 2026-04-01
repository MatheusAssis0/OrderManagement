package com.example.OrderManagement.infra.Exceptions;

public class OrderCannotBePaid extends RuntimeException {
    public OrderCannotBePaid(String message) {
        super(message);
    }
}
