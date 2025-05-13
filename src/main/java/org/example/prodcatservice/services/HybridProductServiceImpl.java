package org.example.prodcatservice.services;

import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.InventoryAuditLog;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.InventoryAuditLogRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HybridProductServiceImpl implements ProductService {

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


    @Override
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.isDeleted()) throw new RuntimeException("Product has been deleted.");
        return product;
    }

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

    @Override
    public void updateStock(Long productId, int quantity, String updatedBy, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int oldStock = product.getStock();

        if (oldStock < quantity) throw new RuntimeException("Insufficient stock.");
        product.setStock(oldStock - quantity);
        productRepository.save(product);
        elasticSearchService.indexProduct(product);

        // Save Inventory Audit Log
        InventoryAuditLog log = InventoryAuditLog.builder()
                .productId(productId)
                .previousQuantity(oldStock)
                .newQuantity(product.getStock())
                .updatedBy(updatedBy)
                .reason(reason != null ? reason : "Stock reduced after order placement")
                .timestamp(Instant.now())
                .build();
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
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .flatMap(cat -> cat.getFeaturedProducts().stream())
                .filter(p -> !p.isDeleted())
                .toList();
    }

    public void setStock(Long productId, int newStock, String updatedBy, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int oldStock = product.getStock();
        product.setStock(newStock);
        productRepository.save(product);
        elasticSearchService.indexProduct(product);

        InventoryAuditLog log = InventoryAuditLog.builder()
                .productId(productId)
                .previousQuantity(oldStock)
                .newQuantity(newStock)
                .updatedBy(updatedBy)
                .reason(reason)
                .timestamp(Instant.now())
                .build();
        inventoryAuditLogRepository.save(log);
    }

}