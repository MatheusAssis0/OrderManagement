package com.example.OrderManagement.service;

import com.example.OrderManagement.dto.ProductDto;
import com.example.OrderManagement.infra.Exceptions.ProductNotFound;
import com.example.OrderManagement.models.Product;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @DisplayName("Should list all products")
    @Test
    void test01() {
        productService.listAllProducts();
        then(productRepository).should().findAll();
    }

    @DisplayName("When register a new product")
    @Nested
    class Create {
        @DisplayName("Then should register successfully")
        @Nested
        class Success {
            @DisplayName("Given a product with all fields filled in correctly")
            @Test
            void test02() {

                //Arrange

                Product product = new Product();
                product.setName("copo");
                product.setPrice(BigDecimal.valueOf(5.00));
                product.setStock(2);
                ProductDto productDto = new ProductDto(product);

                //Act

                productService.createProduct(productDto);

                //Assert

                then(productRepository).should().save(productCaptor.capture());

                Product productSaved = productCaptor.getValue();

                Assertions.assertEquals(product.getName(), productSaved.getName());
                Assertions.assertEquals(product.getPrice(), productSaved.getPrice());
                Assertions.assertEquals(product.getStock(), productSaved.getStock());

            }
        }
    }

    @DisplayName("When updating a product")
    @Nested
    class Update {
        @DisplayName("Then Should update successfully")
        @Nested
        class Success {
            @DisplayName("Given a product id that already exists")
            @Test
            void test03(){

                //Arrange

                Long id = 1L;
                Product product1 = new Product();
                product1.setName("Lápis");
                product1.setStock(1);
                product1.setPrice(BigDecimal.valueOf(9.00));
                ProductDto productDto1 = new ProductDto(product1);

                Product product2 = new Product();
                product2.setName("copo");
                product2.setPrice(BigDecimal.valueOf(5.00));
                product2.setStock(6);

                when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(productRepository.findById(id)).thenReturn(Optional.of(product2));

                //Act

                productService.updateProduct(id , productDto1);

                //Assert

                then(productRepository).should().save(productCaptor.capture());

                Product productSaved = productCaptor.getValue();

                Assertions.assertEquals(product1.getName(), productSaved.getName());
                Assertions.assertEquals(product1.getPrice(), productSaved.getPrice());
                Assertions.assertEquals(product1.getStock(), productSaved.getStock());
            }
        }
        @DisplayName("Then should fail to update")
        @Nested
        class Failure {
            @DisplayName("GIven an invalid ID")
            @Test
            void test04(){

                //Arrange

                Long id = 4L;
                Product product = new Product();
                product.setName("copo");
                product.setPrice(BigDecimal.valueOf(5.00));
                product.setStock(7);
                ProductDto productDto = new ProductDto(product);

                when(productRepository.findById(id)).thenReturn(Optional.empty());

                //Act + Assert

                Assertions.assertThrows(ProductNotFound.class, () -> productService.updateProduct(id, productDto));
                then(productRepository).should(never()).save(any());
            }
        }
    }
}