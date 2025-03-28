package org.example.prodcatservice.services;

import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.models.Product;

import java.util.List;

public interface ProductService {

    public Product createProduct(Product product);
    public List<Product> createProducts(List<CreateProductRequestDto> createProductDtos);
    public Product getProduct(Long id);
    public void deleteProduct(Long id);
//    public Product updateProduct(Long id, Product product);
    public Product partialUpdateProduct(Long id, Product product);
}
