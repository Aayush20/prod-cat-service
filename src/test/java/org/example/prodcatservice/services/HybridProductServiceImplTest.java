package org.example.prodcatservice.services;

import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.InventoryAuditLogRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HybridProductServiceImplTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private InventoryAuditLogRepository inventoryAuditLogRepository;
    private ElasticSearchServiceImpl elasticSearchService;
    private ProductService productService;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        elasticSearchService = mock(ElasticSearchServiceImpl.class);
        inventoryAuditLogRepository = mock(InventoryAuditLogRepository.class);
        productService = new HybridProductServiceImpl(productRepository, categoryRepository, elasticSearchService, inventoryAuditLogRepository);
    }

    @Test
    void testGetProduct_success() {
        Product p = new Product();
        p.setId(1L);
        p.setTitle("Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        Product result = productService.getProduct(1L);
        assertEquals("Product", result.getTitle());
    }

    @Test
    void testGetProduct_notFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.getProduct(1L));
    }

    @Test
    void testCreateProduct_success() {
        Product p = new Product();
        p.setTitle("Phone");
        when(productRepository.save(any())).thenReturn(p);
        Product result = productService.createProduct(p);
        assertEquals("Phone", result.getTitle());
    }

    @Test
    void testUpdateStock_success() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.updateStock(1L, 2, "order-service", "order placed");
        verify(productRepository).save(any());
    }

    @Test
    void testUpdateStock_insufficientStock() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(RuntimeException.class, () -> productService.updateStock(1L, 5, "order-service", "order placed"));
    }
}