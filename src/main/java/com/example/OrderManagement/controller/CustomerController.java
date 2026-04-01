package com.example.OrderManagement.controller;

import com.example.OrderManagement.dto.CustomerDto;
import com.example.OrderManagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> listAllCustomers()
    {
        var response = customerService.listAllCustomers();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto)
    {
        var created = customerService.createCustomer(customerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CustomerDto(created));
    }
}
