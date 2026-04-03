package com.example.OrderManagement.controller;

import com.example.OrderManagement.infra.Exceptions.ProductNotFound;
import com.example.OrderManagement.models.Product;
import com.example.OrderManagement.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService productService;

    @DisplayName("Should return code 200 for listing all products")
    @Test
    void test01() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk());
    }

    @DisplayName("When creating a new product")
    @Nested
    class Create {
        @DisplayName("Then should successfully create")
        @Nested
        class Success {
            @DisplayName("Given a product with all fields filled in correctly")
            @Test
            void test02() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":5.00,
                            "stock":5
                        }
                        """;

                Product product = new Product(1L, "copo", BigDecimal.valueOf(5.00), 5);

                Mockito.when(productService.createProduct(Mockito.any())).thenReturn(product);

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("copo"))
                        .andExpect(jsonPath("$.price").value(5.00))
                        .andExpect(jsonPath("$.stock").value(5));
            }
        }

        @DisplayName("Then should fail")
        @Nested
        class Failure {
            @DisplayName("Given a product without name field filled in")
            @Test
            void test03() throws Exception {

                var json = """
                        {
                            "name":"",
                            "price":5.00,
                            "stock":5
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }

            @DisplayName("Given a product without price field filled in")
            @Test
            void test04() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":null,
                            "stock":5
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }

            @DisplayName("Given a product with a negative price")
            @Test
            void test05() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":-5.00,
                            "stock":5
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }

            @DisplayName("Given a product with price 0")
            @Test
            void test06() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":0.00,
                            "stock":5
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }

            @DisplayName("Given a product without stock field filled in")
            @Test
            void test07() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":5.00,
                            "stock":null
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }

            @DisplayName("Given a product with negative stock")
            @Test
            void test08() throws Exception {

                var json = """
                        {
                            "name":"copo",
                            "price":5.00,
                            "stock":-5
                        }
                        """;

                mvc.perform(post("/products")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).createProduct(Mockito.any());
            }
        }
    }

    @DisplayName("When updating a product")
    @Nested
    class Update {
        @DisplayName("Should update successfully")
        @Nested
        class Success {
            @DisplayName("Given an existing product id with all the fields filled in correctly")
            @Test
            void test09() throws Exception {

                Long id = 1L;
                var json = """
                        {
                            "name":"copo",
                            "price":5.00,
                            "stock":5
                        }
                        """;

                Product product = new Product(id, "copo", BigDecimal.valueOf(5.00), 8);

                Mockito.when(productService.updateProduct(Mockito.anyLong(), Mockito.any())).thenReturn(product);

                mvc.perform(put("/products/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("copo"))
                        .andExpect(jsonPath("$.price").value(5.00))
                        .andExpect(jsonPath("$.stock").value(8));
            }
        }

        @DisplayName("Should fail to update")
        @Nested
        class Failure {
            @DisplayName("Given a non existing product id")
            @Test
            void test10() throws Exception {

                Long id = 1L;
                var json = """
                        {
                            "name":"copo",
                            "price":5.00,
                            "stock":5
                        }
                        """;

                Mockito.when(productService.updateProduct(Mockito.anyLong(), Mockito.any()))
                        .thenThrow(new ProductNotFound("Product not found"));

                mvc.perform(put("/products/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Product not found"));
            }

            @DisplayName("Given a product without all fields filled in")
            @Test
            void test11() throws Exception {

                Long id = 1L;
                var json = """
                        {
                            "name":"",
                            "price":5.00,
                            "stock":-5
                        }
                        """;

                mvc.perform(put("/products/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(productService, Mockito.never()).updateProduct(Mockito.anyLong(), Mockito.any());
            }
        }
    }
}