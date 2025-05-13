package org.example.prodcatservice.services;


import jakarta.ws.rs.NotFoundException;
import org.example.prodcatservice.controllers.ProductController;
import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.RollbackStockRequestDto;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.InventoryAuditLog;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.InventoryAuditLogRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class HybridProductServiceImpl implements ProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ElasticSearchServiceImpl elasticSearchService;
    private final InventoryAuditLogRepository inventoryAuditLogRepository;

    public HybridProductServiceImpl(ProductRepository productRepository,
                                    CategoryRepository categoryRepository,
                                    ElasticSearchServiceImpl elasticSearchService,
                                    InventoryAuditLogRepository inventoryAuditLogRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.elasticSearchService = elasticSearchService;
        this.inventoryAuditLogRepository = inventoryAuditLogRepository;
    }

    @Override
    public Product createProduct(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        if (categoryName != null) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
            product.setCategory(category);
        }
        Product saved = productRepository.save(product);
        elasticSearchService.indexProduct(saved);
        return saved;
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.isDeleted()) throw new RuntimeException("Product has been deleted.");
        return product;
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (product.isDeleted()) return;
        product.setDeleted(true);
        productRepository.save(product);
        elasticSearchService.deleteProductFromIndex(id);
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public Product partialUpdateProduct(Long id, Product updates) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (updates.getTitle() != null) product.setTitle(updates.getTitle());
        if (updates.getDescription() != null) product.setDescription(updates.getDescription());
        if (updates.getImageUrl() != null) product.setImageUrl(updates.getImageUrl());
        if (updates.getSeller() != null) product.setSeller(updates.getSeller());
        if (updates.getStock() > 0) product.setStock(updates.getStock());
        if (updates.getPrice() > 0) product.setPrice(updates.getPrice());

        if (updates.getCategory() != null && updates.getCategory().getName() != null) {
            Category category = categoryRepository.findByName(updates.getCategory().getName())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + updates.getCategory().getName()));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        elasticSearchService.indexProduct(updated);
        return updated;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Override
    public void updateStock(Long productId, int quantity, String updatedBy, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int oldStock = product.getStock();

        if (oldStock < quantity) {
            throw new RuntimeException("Insufficient stock.");
        }

        int newStock = oldStock - quantity;
        product.setStock(newStock);
        productRepository.save(product);
        elasticSearchService.indexProduct(product);

        InventoryAuditLog log = new InventoryAuditLog();
        log.setProductId(productId);
        log.setPreviousQuantity(oldStock);
        log.setNewQuantity(newStock);
        log.setUpdatedBy(updatedBy);
        log.setReason(reason != null ? reason : "Stock reduced after order placement");
        log.setTimestamp(Instant.now());

        inventoryAuditLogRepository.save(log);
    }



    @Override
    public boolean isAvailable(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return !product.isDeleted() && product.getStock() >= quantity;
    }

    @Override
    public List<Product> getFeaturedProducts() {
        String cacheKey = "featured_products";
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Cache hit for featured products");
            return cached;
        }

        log.info("Cache miss for featured products. Fetching from DB...");
        List<Category> categories = categoryRepository.findAll();
        List<Product> featured = categories.stream()
                .flatMap(cat -> cat.getFeaturedProducts().stream())
                .filter(p -> !p.isDeleted())
                .toList();


        redisTemplate.opsForValue().set(cacheKey, featured, 10, TimeUnit.MINUTES);
        return featured;
    }


    @Override
    public void rollbackStock(RollbackStockRequestDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        int oldStock = product.getStock();
        int newStock = oldStock + dto.getQuantity();

        product.setStock(newStock);
        productRepository.save(product);
        elasticSearchService.indexProduct(product);

        InventoryAuditLog log = new InventoryAuditLog();
        log.setProductId(dto.getProductId());
        log.setPreviousQuantity(oldStock);
        log.setNewQuantity(newStock);
        log.setUpdatedBy("system-rollback"); // Or extract from JWT if available
        log.setReason(dto.getReason() != null ? dto.getReason() : "Stock rollback after payment/order failure");
        log.setTimestamp(Instant.now());

        inventoryAuditLogRepository.save(log);
    }



}