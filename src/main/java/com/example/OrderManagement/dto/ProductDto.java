package com.example.OrderManagement.dto;

import com.example.OrderManagement.models.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductDto(Long id, @NotBlank String name, @Positive BigDecimal price, @PositiveOrZero Integer stock) {
    public ProductDto(Product product)
    {
        this(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}
