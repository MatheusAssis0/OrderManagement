package com.example.OrderManagement.repository;

import com.example.OrderManagement.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

   Product findByName(String name);

   Optional<Product> findById(Long id);
}
