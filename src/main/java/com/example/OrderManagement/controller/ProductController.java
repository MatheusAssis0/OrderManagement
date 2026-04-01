package com.example.OrderManagement.controller;

import com.example.OrderManagement.dto.ProductDto;
import com.example.OrderManagement.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> listAllProducts()
    {
        var response = productService.listAllProducts();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto)
    {
        var created = productService.createProduct(productDto);
        var productCreated = new ProductDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(productCreated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto)
    {
        var product = productService.updateProduct(id, productDto);
        var productUpdated = new ProductDto(product);
        return ResponseEntity.ok(productUpdated);
    }
}