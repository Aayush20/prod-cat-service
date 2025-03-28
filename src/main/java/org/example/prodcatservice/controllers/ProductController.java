package org.example.prodcatservice.controllers;

import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.PatchProductRequestDto;
import org.example.prodcatservice.dtos.product.responseDtos.CreateProductResponseDto;
import org.example.prodcatservice.dtos.product.responseDtos.DeleteProductResponseDto;
import org.example.prodcatservice.dtos.product.responseDtos.GetProductResponseDto;
import org.example.prodcatservice.dtos.product.responseDtos.PatchProductResponseDto;
import org.example.prodcatservice.dtos.product.responseDtos.ResponseStatus;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/")
    public CreateProductResponseDto createProduct(@RequestBody CreateProductRequestDto createProductRequestDto,
                                                  @AuthenticationPrincipal Jwt jwt) {
        // Token is validated automatically.
        // Optionally log the user performing the action:
        System.out.println("User " + jwt.getSubject() + " is creating a product.");

        Product product = productService.createProduct(createProductRequestDto.toProduct());
        CreateProductResponseDto responseDto = CreateProductResponseDto.fromProduct(product);
        responseDto.setStatus(ResponseStatus.SUCCESS);
        responseDto.setMessage("Product created successfully");
        return responseDto;
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<CreateProductRequestDto> createProductDtos,
                                                        @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User " + jwt.getSubject() + " is creating multiple products.");
        List<Product> createdProducts = productService.createProducts(createProductDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }

    @GetMapping("/{id}")
    public GetProductResponseDto getProduct(@PathVariable("id") Long id,
                                            @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User " + jwt.getSubject() + " is fetching product with id " + id);
        Product product = productService.getProduct(id);
        GetProductResponseDto responseDto = GetProductResponseDto.fromProduct(product);
        responseDto.setStatus(ResponseStatus.SUCCESS);
        responseDto.setMessage("Product fetched successfully");
        return responseDto;
    }

    @DeleteMapping("/{id}")
    public DeleteProductResponseDto deleteProduct(@PathVariable("id") Long id,
                                                  @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User " + jwt.getSubject() + " is deleting product with id " + id);
        productService.deleteProduct(id);
        DeleteProductResponseDto responseDto = new DeleteProductResponseDto();
        responseDto.setStatus(ResponseStatus.SUCCESS);
        responseDto.setMessage("Product deleted successfully");
        return responseDto;
    }

    @PatchMapping("/{id}")
    public PatchProductResponseDto partialUpdateProduct(@PathVariable("id") Long id,
                                                        @RequestBody PatchProductRequestDto patchProductRequestDto,
                                                        @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User " + jwt.getSubject() + " is updating product with id " + id);
        Product product = productService.partialUpdateProduct(id, patchProductRequestDto.toProduct());
        PatchProductResponseDto responseDto = PatchProductResponseDto.fromProduct(product);
        responseDto.setStatus(ResponseStatus.SUCCESS);
        responseDto.setMessage("Product updated successfully");
        return responseDto;
    }
}
