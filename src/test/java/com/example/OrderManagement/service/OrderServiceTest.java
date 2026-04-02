package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.CreateOrderRequest;
import com.example.OrderManagement.dto.OrderItemRequest;
import com.example.OrderManagement.infra.Exceptions.*;
import com.example.OrderManagement.models.*;
import com.example.OrderManagement.repository.CustomerRepository;
import com.example.OrderManagement.repository.OrderRepository;
import com.example.OrderManagement.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @DisplayName("Should list all orders")
    @Test
    void test01() {
        orderService.listAllOrders();
        then(orderRepository).should().findAll();
    }

    @DisplayName("When list a specific order")
    @Nested
    class listOne {
        @DisplayName("Then should successfully list")
        @Nested
        class Success {
            @DisplayName("Given an existing order ID")
            @Test
            void test02() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act + Assert

                Assertions.assertDoesNotThrow(() -> orderService.listOneOrder(id));
            }
        }

        @DisplayName("Then should throw an error")
        @Nested
        class Failure {
            @DisplayName("Given an non existing order ID")
            @Test
            void test03() {

                //Arrange

                Long id = 1L;
                when(orderRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(OrderNotFound.class, () -> orderService.listOneOrder(id));
            }
        }
    }

    @DisplayName("When try to pay an order")
    @Nested
    class Pay {
        @DisplayName("Then should succeed")
        @Nested
        class Success {
            @DisplayName("Given an existing order that's not paid or cancelled")
            @Test
            void test04() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                order.setStatus(Status.CREATED);
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act

                orderService.payOrder(id);

                //Assert

                then(orderRepository).should().save(orderCaptor.capture());
                Order paidOrder = orderCaptor.getValue();

                Assertions.assertEquals(Status.PAID, paidOrder.getStatus());
            }
        }

        @DisplayName("Then should fail")
        @Nested
        class Failure {
            @DisplayName("Given an non existing order ID")
            @Test
            void test05() {

                //Arrange

                Long id = 1L;
                when(orderRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(OrderNotFound.class, () -> orderService.payOrder(id));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an order already paid")
            @Test
            void test06() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                order.setStatus(Status.PAID);
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act + Assert

                Assertions.assertThrows(OrderCannotBePaid.class, () -> orderService.payOrder(id));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an cancelled order")
            @Test
            void test07() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                order.setStatus(Status.CANCELLED);
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act + Assert

                Assertions.assertThrows(OrderCannotBePaid.class, () -> orderService.payOrder(id));
                then(orderRepository).should(never()).save(any());
            }
        }
    }

    @DisplayName("When try to cancel an order")
    @Nested
    class Cancel {
        @DisplayName("Then should succeed")
        @Nested
        class Success {
            @DisplayName("Given an existing order that's not paid or cancelled")
            @Test
            void test08() {

                //Arrange

                Long id = 1L;

                Product product = new Product();
                product.setStock(5);

                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(3);

                Order order = new Order();
                order.setStatus(Status.CREATED);
                order.setItems(List.of(item));

                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                //Act

                Order result = orderService.cancelOrder(id);

                //Assert

                Assertions.assertEquals(Status.CANCELLED, result.getStatus());

                Assertions.assertEquals(8, product.getStock());

                then(orderRepository).should().save(orderCaptor.capture());

                Order savedOrder = orderCaptor.getValue();
                Assertions.assertEquals(Status.CANCELLED, savedOrder.getStatus());
            }
        }

        @DisplayName("Then should fail")
        @Nested
        class Failure {
            @DisplayName("Given an non existing ID order")
            @Test
            void test09() {

                //Arrange

                Long id = 1L;
                when(orderRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(OrderNotFound.class, () -> orderService.cancelOrder(id));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an already cancelled order")
            @Test
            void test10() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                order.setStatus(Status.CANCELLED);
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act + Assert

                Assertions.assertThrows(OrderStatus.class, () -> orderService.cancelOrder(id));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an order already paid")
            @Test
            void test11() {

                //Arrange

                Long id = 1L;
                Order order = new Order();
                order.setStatus(Status.PAID);
                when(orderRepository.findById(id)).thenReturn(Optional.of(order));

                //Act + Assert

                Assertions.assertThrows(OrderStatus.class, () -> orderService.cancelOrder(id));
                then(orderRepository).should(never()).save(any());
            }
        }
    }

    @DisplayName("When try to create a new order")
    @Nested
    class Create {
        @DisplayName("Then should succeed")
        @Nested
        class Success {
            @DisplayName("Given order with all fields filled in correctly")
            @Test
            void test12(){

                //Arrange

                OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 1);
                List<OrderItemRequest> orderItemRequestList = new ArrayList<>();
                orderItemRequestList.add(orderItemRequest);
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(1L, orderItemRequestList);
                Customer customer = new Customer();
                Product product = new Product();
                product.setStock(2);
                product.setPrice(BigDecimal.valueOf(5.00));
                when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                //Act

                orderService.createOrder(createOrderRequest);

                //Assert

                then(orderRepository).should().save(orderCaptor.capture());
                Order savedOrder = orderCaptor.getValue();

                Assertions.assertEquals(Status.CREATED, savedOrder.getStatus());
                Assertions.assertEquals(BigDecimal.valueOf(5.00), savedOrder.getTotalAmount());
                Assertions.assertEquals(1, savedOrder.getItems().size());
                Assertions.assertEquals(1, product.getStock());
            }
        }

        @DisplayName("Then should fail")
        @Nested
        class Failure {
            @DisplayName("Given an order with a wrong customer ID")
            @Test
            void test13(){

                //Arrange

                Long id = 1L;
                OrderItemRequest orderItemRequest = new OrderItemRequest(id, 2);
                List<OrderItemRequest> orderItemRequestList = new ArrayList<>();
                orderItemRequestList.add(orderItemRequest);
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(id, orderItemRequestList);
                when(customerRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(CustomerNotFound.class, () -> orderService.createOrder(createOrderRequest));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an empty order")
            @Test
            void test14(){

                //Arrange

                Long id = 1L;
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(id, List.of());

                //Act + Assert

                Assertions.assertThrows(EmptyOrder.class, () -> orderService.createOrder(createOrderRequest));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given an invalid item quantity")
            @Test
            void test15(){

                //Arrange

                Long id = 1L;
                Customer customer = new Customer();
                OrderItemRequest orderItemRequest = new OrderItemRequest(id, -2);
                List<OrderItemRequest> orderItemRequestList = new ArrayList<>();
                orderItemRequestList.add(orderItemRequest);
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(id, orderItemRequestList);
                when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

                //Act + Assert

                Assertions.assertThrows(InvalidItemQuantity.class, () -> orderService.createOrder(createOrderRequest));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given a invalid product ID")
            @Test
            void test16(){

                //Arrange

                Long id = 1L;
                Customer customer = new Customer();
                OrderItemRequest orderItemRequest = new OrderItemRequest(id, 2);
                List<OrderItemRequest> orderItemRequestList = new ArrayList<>();
                orderItemRequestList.add(orderItemRequest);
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(id, orderItemRequestList);
                when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
                when(productRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(ProductNotFound.class, () -> orderService.createOrder(createOrderRequest));
                then(orderRepository).should(never()).save(any());
            }

            @DisplayName("Given insufficient stock")
            @Test
            void test17(){

                //Arrange

                Long id = 1L;
                Customer customer = new Customer();
                Product product = new Product();
                product.setStock(1);
                OrderItemRequest orderItemRequest = new OrderItemRequest(id, 2);
                List<OrderItemRequest> orderItemRequestList = new ArrayList<>();
                orderItemRequestList.add(orderItemRequest);
                CreateOrderRequest createOrderRequest = new CreateOrderRequest(id, orderItemRequestList);
                when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
                when(productRepository.findById(id)).thenReturn(Optional.of(product));

                //Act + Assert

                Assertions.assertThrows(InsufficientStock.class, () -> orderService.createOrder(createOrderRequest));
                then(orderRepository).should(never()).save(any());
            }
        }
    }
}
