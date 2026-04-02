package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.CreateOrderRequest;
import com.example.OrderManagement.dto.OrderDto;
import com.example.OrderManagement.dto.OrderItemRequest;
import com.example.OrderManagement.infra.Exceptions.*;
import com.example.OrderManagement.models.*;
import com.example.OrderManagement.repository.CustomerRepository;
import com.example.OrderManagement.repository.OrderRepository;
import com.example.OrderManagement.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {


    private final OrderRepository orderRepository;


    private final ProductRepository productRepository;

    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public List<OrderDto> listAllOrders() {
        return orderRepository.findAll().stream().map(OrderDto::new).toList();
    }

    public OrderDto listOneOrder(Long id) {
        var exists = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound("Order not found"));
        return new OrderDto(exists);
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        if (request.items().isEmpty()) {
            throw new EmptyOrder("Order must have at least one item");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFound("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(Status.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest item : request.items()) {

            if (item.quantity() <= 0) {
                throw new InvalidItemQuantity("Invalid quantity");
            }

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFound("Product not found"));

            if (product.getStock() < item.quantity()) {
                throw new InsufficientStock("Insufficient stock");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.quantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setOrder(order);

            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(item.quantity()))
            );

            product.setStock(product.getStock() - item.quantity());

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        try {
            return orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentUpdateDetected("Concurrent update detected, try again");
        }
    }

    public Order payOrder(Long id) {

        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound("Order not found"));

        if(order.getStatus() != Status.CREATED){
            throw new OrderCannotBePaid("Order cannot be paid");
        }

        order.setStatus(Status.PAID);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long id){

        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound("Order not found"));

        if(order.getStatus() == Status.CANCELLED)
        {
            throw new OrderStatus("Order already cancelled");
        }
        else if (order.getStatus() == Status.PAID){
            throw new OrderStatus("Order already paid");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(Status.CANCELLED);
        return orderRepository.save(order);
    }
}

