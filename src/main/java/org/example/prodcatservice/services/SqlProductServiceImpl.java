package org.example.prodcatservice.services;

import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.CategoryRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SqlProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ElasticSearchServiceImpl elasticSearchService; // Injecting our ES service

    public SqlProductServiceImpl(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 ElasticSearchServiceImpl elasticSearchService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.elasticSearchService = elasticSearchService;
    }

    @Override
    public Product createProduct(Product product) {

        // Persist product in the SQL database.
        Product saved = productRepository.save(product);
        // After the SQL save, index the product in Elasticsearch.
        elasticSearchService.indexProduct(saved);
        return saved;
    }

    public List<Product> createProducts(List<CreateProductRequestDto> createProductDtos) {
        List<Product> products = new ArrayList<>();
        for (CreateProductRequestDto dto : createProductDtos) {
            Product product = new Product();
            product.setTitle(dto.getTitle());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setStock(dto.getStock());
            // Set other fields as needed
            Product created = productRepository.save(product);
            products.add(created);
            elasticSearchService.indexProduct(created);
        }
        return products;
    }


    @Override
    public Product getProduct(Long id) {
        // Retrieve the product and ensure it hasn't been soft-deleted
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent() && !optionalProduct.get().isDeleted()) {
            return optionalProduct.get();
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        // Instead of performing a hard delete, perform a soft delete by updating the isDeleted flag.
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent() && !optionalProduct.get().isDeleted()) {
            Product product = optionalProduct.get();
            product.setDeleted(true);
            productRepository.save(product);
            elasticSearchService.deleteProductFromIndex(id);
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }

    @Override
    public Product partialUpdateProduct(Long id, Product productUpdates) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent() && !optionalProduct.get().isDeleted()) {
            Product productToUpdate = optionalProduct.get();

            // Update only fields that are provided in productUpdates.
            if (productUpdates.getTitle() != null) {
                productToUpdate.setTitle(productUpdates.getTitle());
            }
            if (productUpdates.getDescription() != null) {
                productToUpdate.setDescription(productUpdates.getDescription());
            }
            // Assuming price and stock are primitives with a default value of 0,
            // you might consider using their wrappers if 0 is a valid value.
            if (productUpdates.getPrice() != 0) {
                productToUpdate.setPrice(productUpdates.getPrice());
            }
            if (productUpdates.getStock() != 0) {
                productToUpdate.setStock(productUpdates.getStock());
            }
            if (productUpdates.getSeller() != null) {
                productToUpdate.setSeller(productUpdates.getSeller());
            }
            if (productUpdates.getImageUrl() != null) {
                productToUpdate.setImageUrl(productUpdates.getImageUrl());
            }
            if (productUpdates.getCategory() != null) {
                // Optionally, verify or load the category via categoryRepository here.
                productToUpdate.setCategory(productUpdates.getCategory());
            }

            // The updatedAt field will be managed automatically if you are using JPA auditing.
            Product updatedProduct = productRepository.save(productToUpdate);
            // Update the product in Elasticsearch.
            elasticSearchService.indexProduct(updatedProduct);
            return updatedProduct;
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }
}
