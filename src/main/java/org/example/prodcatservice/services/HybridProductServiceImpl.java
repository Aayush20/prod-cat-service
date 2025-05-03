package org.example.prodcatservice.services;

import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HybridProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ElasticSearchServiceImpl elasticSearchService;

    public HybridProductServiceImpl(ProductRepository productRepository,
                                    CategoryRepository categoryRepository,
                                    ElasticSearchServiceImpl elasticSearchService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.elasticSearchService = elasticSearchService;
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
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < quantity) throw new RuntimeException("Insufficient stock.");
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        elasticSearchService.indexProduct(product);
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
}