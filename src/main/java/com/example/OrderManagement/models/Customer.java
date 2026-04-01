package com.example.OrderManagement.models;

import com.example.OrderManagement.dto.CustomerDto;
import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table (name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    public Customer(){}

    public Customer(CustomerDto customerDto)
    {
        this.name = customerDto.name();
        this.email = customerDto.email();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
