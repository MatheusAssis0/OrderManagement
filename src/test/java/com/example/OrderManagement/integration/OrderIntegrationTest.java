package com.example.OrderManagement.integration;

import com.example.OrderManagement.AbstractIntegrationTest;
import com.example.OrderManagement.models.Customer;
import com.example.OrderManagement.models.Product;
import com.example.OrderManagement.repository.CustomerRepository;
import com.example.OrderManagement.repository.OrderRepository;
import com.example.OrderManagement.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
    }

    @DisplayName("Should list all orders")
    @Test
    void test01() throws Exception {
        Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

        Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

        var json = """
                {
                  "customerId": %d,
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 3
                    }
                  ]
                }
                """.formatted(customer.getId(), product.getId());

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer.id").value(customer.getId()))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].totalAmount").value(15));

        assertEquals(7, productRepository.findById(product.getId()).get().getStock());
    }

    @DisplayName("When creating a new order")
    @Nested
    class Create {
        @DisplayName("Should successfully create")
        @Nested
        class Success {
            @DisplayName("Given a order with all fields filled in correctly")
            @Test
            void test02() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 3
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                        .andExpect(jsonPath("$.customer.email").value(customer.getEmail()))
                        .andExpect(jsonPath("$.customer.name").value(customer.getName()))
                        .andExpect(jsonPath("$.status").value("CREATED"))
                        .andExpect(jsonPath("$.totalAmount").value(15.00));

                var order = orderRepository.findAll().get(0);
                assertEquals(15.00, order.getTotalAmount().doubleValue());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(7, updatedProduct.getStock());
            }
        }

        @DisplayName("Should fail to create")
        @Nested
        class Failure {
            @DisplayName("Given a non existing customer ID")
            @Test
            void test03() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 3
                            }
                          ]
                        }
                        """.formatted(Long.MAX_VALUE, product.getId());

                mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Customer not found"));

                assertTrue(orderRepository.findAll().isEmpty());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }

            @DisplayName("Given a non existing product ID")
            @Test
            void test04() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 3
                            }
                          ]
                        }
                        """.formatted(customer.getId(), Long.MAX_VALUE);

                mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Product not found"));

                assertTrue(orderRepository.findAll().isEmpty());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }

            @DisplayName("Given a quantity > stock")
            @Test
            void test05() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 12
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("Insufficient stock"));

                assertTrue(orderRepository.findAll().isEmpty());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }

            @DisplayName("Given an empty order")
            @Test
            void test06() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                          ]
                        }
                        """.formatted(customer.getId());

                mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$[0].message").value("must not be empty"));

                assertTrue(orderRepository.findAll().isEmpty());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }

            @DisplayName("Given a order with item quantity <= 0")
            @Test
            void test07() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 0
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Invalid quantity"));

                assertTrue(orderRepository.findAll().isEmpty());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }
        }
    }

    @DisplayName("When paying a order")
    @Nested
    class Pay {
        @DisplayName("Should successfully pay")
        @Nested
        class Success {
            @DisplayName("Given a existing order ID with status = CREATED")
            @Test
            void test08() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/pay", orderId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("PAID"));

                var updatedOrder = orderRepository.findById(orderId).get();
                assertEquals("PAID", updatedOrder.getStatus().name());
            }
        }

        @DisplayName("Should fail to pay")
        @Nested
        class Failure {
            @DisplayName("Given a non existing order ID")
            @Test
            void test09() throws Exception {

                mockMvc.perform(post("/orders/{id}/pay", Long.MAX_VALUE))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Order not found"));
            }

            @DisplayName("Given a already paid order")
            @Test
            void test10() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated()).andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/pay", orderId))
                        .andExpect(status().isOk());

                mockMvc.perform(post("/orders/{id}/pay", orderId))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order cannot be paid"));
            }

            @DisplayName("Given a cancelled order")
            @Test
            void test11() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated()).andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/cancel", orderId))
                        .andExpect(status().isOk());

                mockMvc.perform(post("/orders/{id}/pay", orderId))
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
            @DisplayName("Given an existing order ID with status = CREATED")
            @Test
            void test12() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/cancel", orderId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("CANCELLED"));

                var updatedOrder = orderRepository.findById(orderId).get();
                assertEquals("CANCELLED", updatedOrder.getStatus().name());
                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }
        }

        @DisplayName("Then should fail to cancel")
        @Nested
        class Failure {
            @DisplayName("Given a non existing order ID")
            @Test
            void test13() throws Exception {

                mockMvc.perform(post("/orders/{id}/cancel", Long.MAX_VALUE))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Order not found"));
            }

            @DisplayName("Given a order already paid")
            @Test
            void test14() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/pay", orderId))
                        .andExpect(status().isOk());

                mockMvc.perform(post("/orders/{id}/cancel", orderId))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order already paid"));

                var updatedOrder = orderRepository.findById(orderId).get();
                assertEquals("PAID", updatedOrder.getStatus().name());

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(2, updatedProduct.getStock());
            }

            @DisplayName("Given a order already cancelled")
            @Test
            void test15() throws Exception {

                Customer customer = customerRepository.save(new Customer(null, "Matheus", "matheus@gmail.com"));

                Product product = productRepository.save(new Product(null, "Copo", BigDecimal.valueOf(5.00), 10));

                var json = """
                        {
                          "customerId": %d,
                          "items": [
                            {
                              "productId": %d,
                              "quantity": 8
                            }
                          ]
                        }
                        """.formatted(customer.getId(), product.getId());

                var order = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andReturn();

                String response = order.getResponse().getContentAsString();
                Integer idNumber = com.jayway.jsonpath.JsonPath.read(response, "$.id");
                Long orderId = idNumber.longValue();

                mockMvc.perform(post("/orders/{id}/cancel", orderId))
                        .andExpect(status().isOk());

                mockMvc.perform(post("/orders/{id}/cancel", orderId))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Order already cancelled"));

                var updatedProduct = productRepository.findById(product.getId()).get();
                assertEquals(10, updatedProduct.getStock());
            }
        }
    }
}

