package org.example.prodcatservice.services;

import org.example.prodcatservice.dtos.product.requestDtos.RollbackStockRequestDto;
import org.example.prodcatservice.models.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProduct(Long id);
    void deleteProduct(Long id);
    Product partialUpdateProduct(Long id, Product product);
    void updateStock(Long productId, int quantity, String updatedBy, String reason);

    boolean isAvailable(Long id, int quantity);
    List<Product> getFeaturedProducts();

    void rollbackStock(RollbackStockRequestDto dto);

}
