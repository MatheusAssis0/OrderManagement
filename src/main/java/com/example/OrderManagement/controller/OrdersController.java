package com.example.OrderManagement.controller;

import com.example.OrderManagement.dto.CreateOrderRequest;
import com.example.OrderManagement.dto.OrderDto;
import com.example.OrderManagement.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> listAllOrders(){
        var response = orderService.listAllOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> listSpecificOrder(@PathVariable Long id) {
        var response = orderService.listOneOrder(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request){
        var order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderDto(order));
    }

    @PostMapping("{id}/pay")
    public ResponseEntity<OrderDto> payOrder(@PathVariable Long id){
        var order = orderService.payOrder(id);
        return ResponseEntity.ok(new OrderDto(order));
    }

    @PostMapping("{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id){
        var order = orderService.cancelOrder(id);
        return ResponseEntity.ok(new OrderDto(order));
    }
}