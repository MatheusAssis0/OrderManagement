package com.example.OrderManagement.dto;

import com.example.OrderManagement.models.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerDto(@NotBlank String name, @NotBlank @Email String email) {
    public CustomerDto(Customer customer){
        this(customer.getName(), customer.getEmail());
    }
}
