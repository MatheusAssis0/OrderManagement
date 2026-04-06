package com.example.OrderManagement.integration;

import com.example.OrderManagement.AbstractIntegrationTest;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.models.Product;
import com.example.OrderManagement.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    @DisplayName("Should list all products")
    @Test
    void test01() throws Exception {

        productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

        productRepository.save(new Product(null, "Lapis", BigDecimal.valueOf(2.00),7));

        mockMvc.perform(get("/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @DisplayName("When creating a new product")
    @Nested
    class Create {
        @DisplayName("Then should successfully create")
        @Nested
        class Success {
            @DisplayName("Given a product with all fields correctly filled in")
            @Test
            void test02() throws Exception {

                var json = """
                        {
                             "name":"Copo",
                             "price":5.00,
                             "stock":5
                        }
                        """;

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Copo"))
                        .andExpect(jsonPath("$.stock").value(5));

                var products = productRepository.findAll();

                assertEquals(1, products.size());
                assertEquals("Copo", products.get(0).getName());
            }
        }

        @DisplayName("Then should fail to create")
        @Nested
        class Failure {
            @DisplayName("Given a product with negative stock")
            @Test
            void test03() throws Exception {

                var json = """
                        {
                             "name":"Copo",
                             "price":5.00,
                             "stock":-5
                        }
                        """;

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$[*].field", hasItem("stock")));

                assertTrue(productRepository.findAll().isEmpty());
            }

            @DisplayName("Given a product with a invalid price")
            @Test
            void test04() throws Exception {

                var json = """
                        {
                             "name":"Copo",
                             "price":0.00,
                             "stock":5
                        }
                        """;

                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$[*].field", hasItem("price")));

                assertTrue(productRepository.findAll().isEmpty());
            }
        }
    }

    @DisplayName("When updating a product")
    @Nested
    class Update {
        @DisplayName("Then should successfully update")
        @Nested
        class Success {
            @DisplayName("Given an existing product with all fields correctly filled in")
            @Test
            void test06() throws Exception {

                var json1 = """
                        {
                             "name":"Copo",
                             "price":5.00,
                             "stock":5
                        }
                        """;

                var json2 = """
                        {
                             "name":"Lapis",
                             "price":2.00,
                             "stock":5
                        }
                        """;

                var result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Copo"))
                        .andReturn();

                String response = result.getResponse().getContentAsString();

                Integer idNumber = JsonPath.read(response, "$.id");
                Long id = idNumber.longValue();

                mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Lapis"));

                var products = productRepository.findAll();

                assertEquals(1, products.size());
                assertEquals("Lapis", products.get(0).getName());
            }
        }

        @DisplayName("Then should fail to update")
        @Nested
        class Failure {
            @DisplayName("Given a non existing product ID")
            @Test
            void test07() throws Exception {

                var json = """
                        {
                             "name":"Copo",
                             "price":5.00,
                             "stock":5
                        }
                        """;

                mockMvc.perform(put("/products/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Product not found"));

                assertTrue(productRepository.findAll().isEmpty());
            }

        }
    }
}
