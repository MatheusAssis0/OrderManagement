package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.ProductDto;
import com.example.OrderManagement.infra.Exceptions.ProductNotFound;
import com.example.OrderManagement.models.Product;
import com.example.OrderManagement.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDto> listAllProducts(){
        var products = productRepository.findAll();
        return products.stream().map(ProductDto::new).toList();
    }

    public Product createProduct(ProductDto productDto)
    {
        var product = new Product(productDto);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id,ProductDto productDto) {

        Product existing = productRepository.findById(id).orElseThrow(() -> new ProductNotFound("Product not found"));
        existing.setName(productDto.name());
        existing.setPrice(productDto.price());
        existing.setStock(productDto.stock());

        return productRepository.save(existing);
    }
}
