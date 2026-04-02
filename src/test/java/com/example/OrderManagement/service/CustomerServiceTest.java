package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.CustomerDto;
import com.example.OrderManagement.infra.Exceptions.EmailAlreadyUsedException;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @DisplayName("Should list all customers")
    @Test
    void test01() {
        customerService.listAllCustomers();
        then(customerRepository).should().findAll();
    }

    @DisplayName("When register a new customer")
    @Nested
    class Create {
        @DisplayName("Then should register successfully")
        @Nested
        class Success {
            @DisplayName("Given a customer with all fields filled in correctly")
            @Test
            void test02() {

                //Arrange

                Customer customer = new Customer();
                customer.setName("Maria");
                customer.setEmail("Maria@gmail.com");

                CustomerDto customerDto = new CustomerDto(customer);

                //Act

                customerService.createCustomer(customerDto);

                //Assert

                then(customerRepository).should().save(customerCaptor.capture());
                Assertions.assertDoesNotThrow(() -> EmailAlreadyUsedException.class);

                Customer customerSaved = customerCaptor.getValue();

                Assertions.assertEquals(customer.getName(), customerSaved.getName());
                Assertions.assertEquals(customer.getEmail(), customerSaved.getEmail());
            }
        }
        @DisplayName("Then should fail")
        @Nested
        class Failure {
            @DisplayName("When registering with an email already registered")
            @Test
            void test03(){

                //Arrange

                Customer customer1 = new Customer();
                Customer customer2 = new Customer();
                customer1.setName("Maria");
                customer1.setEmail("Maria@gmail.com");
                customer2.setName("Mari");
                customer2.setEmail("Maria@gmail.com");

                CustomerDto customerDto1 = new CustomerDto(customer1);
                CustomerDto customerDto2 = new CustomerDto(customer2);

                when(customerRepository.existsByEmail("Maria@gmail.com")).thenReturn(false).thenReturn(true);

                //Act

                customerService.createCustomer(customerDto1);

                //Assert

                Assertions.assertThrows(EmailAlreadyUsedException.class, () -> customerService.createCustomer(customerDto2));
            }
        }
    }

}



