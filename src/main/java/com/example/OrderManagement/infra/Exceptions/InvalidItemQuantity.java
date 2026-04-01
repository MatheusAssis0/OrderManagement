package com.example.OrderManagement.infra.Exceptions;

public class InvalidItemQuantity extends RuntimeException {
    public InvalidItemQuantity(String message) {
        super(message);
    }
}
