package com.example.OrderManagement.infra.Exceptions;

public class EmailAlreadyUsedException  extends RuntimeException{
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
