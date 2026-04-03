package com.example.OrderManagement.models;

import com.example.OrderManagement.dto.ProductDto;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    @Version
    private Long version;

    public Product (ProductDto productDto)
    {
        this.name = productDto.name();
        this.price = productDto.price();
        this.stock = productDto.stock();
    }

    public Product(){}

    public Product(Long id, String name, BigDecimal price, Integer stock){
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
