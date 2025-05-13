package org.example.prodcatservice.services;

import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.InventoryAuditLogRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private InventoryAuditLogRepository inventoryAuditLogRepository;
    private ElasticSearchServiceImpl elasticSearchService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        elasticSearchService = mock(ElasticSearchServiceImpl.class);
        inventoryAuditLogRepository = mock(InventoryAuditLogRepository.class);
        productService = new HybridProductServiceImpl(productRepository, categoryRepository, elasticSearchService, inventoryAuditLogRepository);
    }

    @Test
    void testGetProduct_Success() {
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTitle("Mock Product");

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        Product result = productService.getProduct(1L);
        assertEquals("Mock Product", result.getTitle());
    }

    @Test
    void testGetProduct_NotFound() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.getProduct(2L));
    }
}
