package com.example.OrderManagement.integration;

import com.example.OrderManagement.AbstractIntegrationTest;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();
    }

    @DisplayName("Should list all customers")
    @Test
    void test01() throws Exception {

        customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

        customerRepository.save(new Customer(null, "João", "joao@gmail.com"));

        mockMvc.perform(get("/customers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @DisplayName("Should successfully create customer")
    @Test
    void test02() throws Exception {

        String json = """
        {
            "name": "Matheus",
            "email": "matheus@gmail.com"
        }
        """;

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Matheus"))
                .andExpect(jsonPath("$.email").value("matheus@gmail.com"));

        var customer = customerRepository.findAll();

        Assertions.assertEquals(customer.size(), 1);
        Assertions.assertEquals(customer.get(0).getName(), "Matheus");
    }

    @DisplayName("Should fail to create a customer with a email already used")
    @Test
    void test03() throws Exception {

        String json = """
                {
                    "name": "Matheus",
                    "email": "matheus@gmail.com"
                }
                """;


        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }
}
