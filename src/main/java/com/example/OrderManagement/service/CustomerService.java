package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.CustomerDto;
import com.example.OrderManagement.infra.Exceptions.EmailAlreadyUsedException;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerDto> listAllCustomers()
    {
        return customerRepository.findAll().stream().map(CustomerDto::new).toList();
    }

    public Customer createCustomer(CustomerDto customerDto)
    {
        if(customerRepository.existsByEmail(customerDto.email()))
        {
            throw new EmailAlreadyUsedException("Email already in use");
        }

        Customer customer = new Customer(customerDto);
        return customerRepository.save(customer);
    }
}
