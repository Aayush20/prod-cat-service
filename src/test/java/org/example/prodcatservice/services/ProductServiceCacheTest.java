package org.example.prodcatservice.services;

import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceCacheTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testGetProductCachesResult() {
        Long productId = 1L;

        // Mock the repository
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setTitle("Cached Product");

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // First call → from DB
        Product result1 = productService.getProduct(productId);
        assertEquals("Cached Product", result1.getTitle());

        // Second call → should come from cache (no DB hit expected)
        Product result2 = productService.getProduct(productId);
        assertEquals("Cached Product", result2.getTitle());

        // Verify DB was only called once
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    public void testCacheEvictOnUpdateStock() {
        Long productId = 2L;

        Product product = new Product();
        product.setId(productId);
        product.setStock(10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Cache product once
        productService.getProduct(productId);
        assertNotNull(cacheManager.getCache("products").get(productId));

        // Call updateStock → evicts cache
        productService.updateStock(productId, 2, "test-user", "unit test");
        assertNull(cacheManager.getCache("products").get(productId)); // Evicted
    }
}
