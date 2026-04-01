package com.example.OrderManagement.models;

import com.example.OrderManagement.dto.OrderDto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();


    public Order() {
    }

    public Order(OrderDto orderDto){
        this.id = orderDto.id();
        this.customer = orderDto.customer();
        this.createdAt = orderDto.createdAt();
        this.status = orderDto.status();
        this.totalAmount = orderDto.totalAmount();
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
