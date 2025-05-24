package org.example.prodcatservice.services;

import jakarta.ws.rs.NotFoundException;
import org.example.prodcatservice.dtos.product.requestDtos.RollbackStockRequestDto;
import org.example.prodcatservice.kafka.KafkaPublisher;
import org.example.prodcatservice.models.*;
import org.example.prodcatservice.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HybridProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private InventoryAuditLogRepository inventoryAuditLogRepository;
    @Mock
    private ElasticSearchServiceImpl elasticSearchService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private KafkaPublisher kafkaPublisher;
    @Mock
    private EmailAlertService emailAlertService;

    @InjectMocks
    private HybridProductServiceImpl productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        productService = new HybridProductServiceImpl(productRepository, categoryRepository, elasticSearchService, inventoryAuditLogRepository);
        ReflectionTestUtils.setField(productService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(productService, "kafkaPublisher", kafkaPublisher);
        ReflectionTestUtils.setField(productService, "emailAlertService", emailAlertService);
        ReflectionTestUtils.setField(productService, "stockThreshold", 3);
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
    void testGetProduct_deleted() {
        Product p = new Product();
        p.setDeleted(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
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
    void testCreateProductWithCategory() {
        Product p = new Product();
        Category c = new Category();
        c.setName("Electronics");
        p.setCategory(c);
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(c));
        when(productRepository.save(any())).thenReturn(p);

        Product result = productService.createProduct(p);
        assertEquals("Electronics", result.getCategory().getName());
    }

    @Test
    void testUpdateStock_success() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.updateStock(1L, 2, "test-user", "test-reason");
        verify(productRepository).save(p);
        verify(kafkaPublisher).publishStockUpdatedEvent(any());
        verify(inventoryAuditLogRepository).save(any());
    }

    @Test
    void testUpdateStock_lowStockTriggersEmail() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(4);
        p.setTitle("T-Shirt");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.updateStock(1L, 2, "test-user", "test-reason");
        verify(emailAlertService).sendLowStockAlert(eq("T-Shirt"), eq(1L), eq(2));
    }

    @Test
    void testUpdateStock_insufficient() {
        Product p = new Product();
        p.setStock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(RuntimeException.class, () -> productService.updateStock(1L, 2, "user", "reason"));
    }

    @Test
    void testDeleteProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setDeleted(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.deleteProduct(1L);
        assertTrue(p.isDeleted());
        verify(productRepository).save(p);
    }

    @Test
    void testDeleteProduct_alreadyDeleted() {
        Product p = new Product();
        p.setDeleted(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.deleteProduct(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void testPartialUpdateProduct() {
        Product original = new Product();
        original.setId(1L);
        original.setTitle("Old");
        original.setPrice(100);

        Product updates = new Product();
        updates.setTitle("New");
        updates.setPrice(200);

        when(productRepository.findById(1L)).thenReturn(Optional.of(original));
        when(productRepository.save(any())).thenReturn(original);

        Product result = productService.partialUpdateProduct(1L, updates);
        assertEquals("New", result.getTitle());
        assertEquals(200, result.getPrice());
    }

    @Test
    void testPartialUpdateWithNewCategory() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setTitle("Laptop");

        Product update = new Product();
        Category newCategory = new Category();
        newCategory.setName("Computers");
        update.setCategory(newCategory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Computers")).thenReturn(Optional.of(newCategory));
        productService.partialUpdateProduct(1L, update);
        verify(categoryRepository).findByName("Computers");
    }

    @Test
    void testIsAvailable_true() {
        Product p = new Product();
        p.setDeleted(false);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertTrue(productService.isAvailable(1L, 5));
    }

    @Test
    void testIsAvailable_false() {
        Product p = new Product();
        p.setDeleted(true);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertFalse(productService.isAvailable(1L, 5));
    }

    @Test
    void testRollbackStock_singleProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        RollbackStockRequestDto dto = new RollbackStockRequestDto();
        RollbackStockRequestDto.ProductRollbackEntry entry = new RollbackStockRequestDto.ProductRollbackEntry();
        entry.setProductId(1L);
        entry.setQuantity(2);
        dto.setProducts(List.of(entry));
        dto.setReason("order failure");

        productService.rollbackStock(dto);

        assertEquals(7, p.getStock());
        verify(productRepository).save(p);
        verify(kafkaPublisher).publishStockRollbackEvent(any());
    }
}
