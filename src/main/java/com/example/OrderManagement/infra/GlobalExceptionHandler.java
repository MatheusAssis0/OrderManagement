package com.example.OrderManagement.infra;

import com.example.OrderManagement.infra.Exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity Error404()
    {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity Error400(MethodArgumentNotValidException ex)
    {
        var errors = ex.getFieldErrors();
        return ResponseEntity.badRequest().body(errors.stream().map(ErrorValidation::new).toList());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity EmailError(EmailAlreadyUsedException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFound.class)
    public ResponseEntity ProductNotFound(ProductNotFound ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(OrderCannotBePaid.class)
    public ResponseEntity OrderCannotBePaid(OrderCannotBePaid ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFound.class)
    public ResponseEntity OrderNotFound(OrderNotFound ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFound.class)
    public ResponseEntity CustomerNotFound(CustomerNotFound ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidItemQuantity.class)
    public ResponseEntity InvalidItemQuantity(InvalidItemQuantity ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EmptyOrder.class)
    public ResponseEntity EmptyOrder(EmptyOrder ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStock.class)
    public ResponseEntity InsufficientStock(InsufficientStock ex)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConcurrentUpdateDetected.class)
    public ResponseEntity ConcurrentUpdateDetected(ConcurrentUpdateDetected ex)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(OrderStatus.class)
    public ResponseEntity OrderStatus(OrderStatus ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    private record ErrorValidation(String field, String message){
        public ErrorValidation(FieldError error){
            this(error.getField(), error.getDefaultMessage());
        }
    }
}