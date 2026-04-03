package com.example.OrderManagement.controller;

import com.example.OrderManagement.infra.Exceptions.EmailAlreadyUsedException;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomerService customerService;

    @DisplayName("Should return code 200 for listing all customers")
    @Test
    void test01() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().isOk());
    }

    @DisplayName("When creating a new customer")
    @Nested
    class Create {
        @DisplayName("Then should successfully create")
        @Nested
        class Success {
            @DisplayName("Given a customer with all fields filled in correctly")
            @Test
            void test02() throws Exception {

                var json = """
                {
                   "name":"a",
                   "email":"a@gmail.com"
                }
                """;

                var customer = new Customer(1L, "a", "a@gmail.com");

                Mockito.when(customerService.createCustomer(Mockito.any())).thenReturn(customer);

                mvc.perform(post("/customers")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("a"))
                        .andExpect(jsonPath("$.email").value("a@gmail.com"));
            }
        }

        @DisplayName("Then should fail to create")
        @Nested
        class Failure {
            @DisplayName("Given a customer with an invalid email")
            @Test
            void test03() throws Exception {

                var json = """
                {
                   "name":"a",
                   "email":"a"
                }
                """;

                mvc.perform(post("/customers")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(customerService, Mockito.never()).createCustomer(Mockito.any());
            }

            @DisplayName("Given a customer without name field filled")
            @Test
            void test04() throws Exception {

                var json = """
                {
                   "name":"",
                   "email":"a"
                }
                """;

                mvc.perform(post("/customers")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(customerService, Mockito.never()).createCustomer(Mockito.any());
            }

            @DisplayName("Given a customer without email field filled")
            @Test
            void test05() throws Exception {

                var json = """
                {
                   "name":"a",
                   "email":""
                }
                """;

                mvc.perform(post("/customers")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(customerService, Mockito.never()).createCustomer(Mockito.any());
            }

            @DisplayName("Given a customer with an email already used")
            @Test
            void test06() throws Exception {

                var json = """
                {
                   "name":"a",
                   "email":"a@gmail.com"
                }
                """;

                Mockito.when(customerService.createCustomer(Mockito.any()))
                        .thenThrow(new EmailAlreadyUsedException("Email already used"));

                mvc.perform(post("/customers")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("Email already used"));

                Mockito.verify(customerService).createCustomer(Mockito.any());
            }
        }
    }
}
