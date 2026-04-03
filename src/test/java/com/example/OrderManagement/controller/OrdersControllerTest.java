package com.example.OrderManagement.controller;

import com.example.OrderManagement.dto.OrderDto;
import com.example.OrderManagement.infra.Exceptions.*;
import com.example.OrderManagement.models.Order;
import com.example.OrderManagement.service.OrderService;
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

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orderService;

    @DisplayName("Should return code 200 for listing all orders")
    @Test
    void test01() throws Exception {
        mvc.perform(get("/orders")).andExpect(status().isOk());
    }

    @DisplayName("When listing an specific order")
    @Nested
    class listOne {
        @DisplayName("Then should successfully list")
        @Nested
        class Success {
            @DisplayName("Given an existing order ID")
            @Test
            void test02() throws Exception {

                Long id = 1L;
                Order order = new Order();
                order.setId(id);

                Mockito.when(orderService.listOneOrder(id)).thenReturn(new OrderDto(order));

                mvc.perform(get("/orders/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1));
            }
        }

        @DisplayName("Then should fail to list")
        @Nested
        class Failure {
            @DisplayName("Given a non existing order ID")
            @Test
            void test03() throws Exception {

                Mockito.when(orderService.listOneOrder(1L)).thenThrow(new OrderNotFound("Order not found"));

                mvc.perform(get("/orders/{id}", 1L))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Order not found"));
            }
        }
    }

    @DisplayName("When creating a new order")
    @Nested
    class Create {
        @DisplayName("Then should successfully create")
        @Nested
        class Success {
            @DisplayName("Given a order with all fields filled in correctly")
            @Test
            void test04() throws Exception {

                var json = """
                        {
                          "customerId": 1,
                          "items": [
                            {
                              "productId": 10,
                              "quantity": 2
                            }
                          ]
                        }
                        """;

                Order order = new Order();
                order.setId(1L);

                Mockito.when(orderService.createOrder(Mockito.any())).thenReturn(order);

                mvc.perform(post("/orders")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists());
            }
        }

        @DisplayName("Then shoul fail to create")
        @Nested
        class Failure {
            @DisplayName("Given a order with a non existing customer ID")
            @Test
            void test05() throws Exception {

                var json = """
                        {
                          "customerId": 1,
                          "items": [
                            {
                              "productId": 10,
                              "quantity": 2
                            }
                          ]
                        }
                        """;

                Mockito.when(orderService.createOrder(Mockito.any()))
                        .thenThrow(new CustomerNotFound("Customer not found"));

                mvc.perform(post("/orders")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Customer not found"));
            }

            @DisplayName("Given an empty list of items")
            @Test
            void test06() throws Exception {

                var json = """
                        {
                          "customerId": 1,
                          "items": []
                        }
                        """;

                mvc.perform(post("/orders")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());

                Mockito.verify(orderService, Mockito.never()).createOrder(Mockito.any());
            }

            @DisplayName("Given a non existing product")
            @Test
            void test07() throws Exception {

                var json = """
                        {
                          "customerId": 1,
                          "items": [
                            {
                              "productId": 999,
                              "quantity": 2
                            }
                          ]
                        }
                        """;

                Mockito.when(orderService.createOrder(Mockito.any()))
                        .thenThrow(new ProductNotFound("Product not found"));

                mvc.perform(post("/orders")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Product not found"));
            }
        }
    }

    @DisplayName("When paying a order")
    @Nested
    class Pay {
        @DisplayName("Then should successfully pay")
        @Nested
        class Success {
            @DisplayName("Given an existing order ID not paid yet")
            @Test
            void test08() throws Exception {

                Long id = 1L;
                Order order = new Order();

                Mockito.when(orderService.payOrder(id)).thenReturn(order);

                mvc.perform(post("/orders/{id}/pay", id))
                        .andExpect(status().isOk());

            }
        }

        @DisplayName("Then should fail to pay")
        @Nested
        class Failure {
            @DisplayName("Given a non existing order ID")
            @Test
            void test09() throws Exception {

                Long id = 1L;

                Mockito.when(orderService.payOrder(id)).thenThrow(new OrderNotFound("Order not found"));

                mvc.perform(post("/orders/{id}/pay", id))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Order not found"));
            }

            @DisplayName("Given an already paid/cancelled order")
            @Test
            void test10() throws Exception {

                Long id = 1L;

                Mockito.when(orderService.payOrder(id)).thenThrow(new OrderCannotBePaid("Order cannot be paid"));

                mvc.perform(post("/orders/{id}/pay", id))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order cannot be paid"));
            }
        }
    }

    @DisplayName("When cancelling a order")
    @Nested
    class Cancel {
        @DisplayName("Then should successfully cancel")
        @Nested
        class Success {
            @DisplayName("Given an existing order ID not cancelled or paid yet")
            @Test
            void test11() throws Exception {

                Long id = 1L;
                Order order = new Order();

                Mockito.when(orderService.cancelOrder(id)).thenReturn(order);

                mvc.perform(post("/orders/{id}/cancel", id)).andExpect(status().isOk());
            }
        }

        @DisplayName("Then should fail to cancel")
        @Nested
        class Failure {
            @DisplayName("Given a non existing order ID")
            @Test
            void test12() throws Exception {

                Long id = 1L;

                Mockito.when(orderService.cancelOrder(id)).thenThrow(new OrderNotFound("Order not found"));

                mvc.perform(post("/orders/{id}/cancel", id))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Order not found"));
            }

            @DisplayName("Given a order already paid")
            @Test
            void test13() throws Exception {

                Long id = 1L;

                Mockito.when(orderService.cancelOrder(id)).thenThrow(new OrderStatus("Order already paid"));

                mvc.perform(post("/orders/{id}/cancel", id))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order already paid"));
            }

            @DisplayName("Given a order already cancelled")
            @Test
            void test14() throws Exception {

                Long id = 1L;

                Mockito.when(orderService.cancelOrder(id)).thenThrow(new OrderStatus("Order already cancelled"));

                mvc.perform(post("/orders/{id}/cancel", id))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order already cancelled"));
            }
        }
    }
}