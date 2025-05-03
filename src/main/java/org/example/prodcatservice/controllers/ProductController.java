package org.example.prodcatservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.PatchProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.UpdateStockRequestDto;
import org.example.prodcatservice.dtos.product.responseDtos.*;
import org.example.prodcatservice.dtos.product.responseDtos.ResponseStatus;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@Tag(name = "Product APIs")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a new product")
    @PostMapping("/")
    public ResponseEntity<CreateProductResponseDto> createProduct(@RequestBody CreateProductRequestDto createProductRequestDto,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is creating a product.", jwt.getSubject());
        Product product = createProductRequestDto.toProduct();
        Product saved = productService.createProduct(product);
        CreateProductResponseDto responseDto = CreateProductResponseDto.fromProduct(saved);
        responseDto.setStatus(ResponseStatus.SUCCESS);
        responseDto.setMessage("Product created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<GetProductResponseDto> getProduct(@PathVariable("id") Long id,
                                                            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is fetching product with id {}", jwt.getSubject(), id);
        Product product = productService.getProduct(id);
        GetProductResponseDto response = GetProductResponseDto.fromProduct(product);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Product fetched successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete product by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProductResponseDto> deleteProduct(@PathVariable("id") Long id,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is deleting product with id {}", jwt.getSubject(), id);
        productService.deleteProduct(id);
        DeleteProductResponseDto response = new DeleteProductResponseDto();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Product deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update a product")
    @PatchMapping("/{id}")
    public ResponseEntity<PatchProductResponseDto> partialUpdateProduct(@PathVariable("id") Long id,
                                                                        @RequestBody PatchProductRequestDto patchProductRequestDto,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is updating product with id {}", jwt.getSubject(), id);
        Product updated = productService.partialUpdateProduct(id, patchProductRequestDto.toProduct());
        PatchProductResponseDto response = PatchProductResponseDto.fromProduct(updated);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Product updated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update product stock (for order-service only)")
    @PatchMapping("/update-stock")
    public ResponseEntity<String> updateStock(@RequestBody UpdateStockRequestDto dto,
                                              @AuthenticationPrincipal Jwt jwt) {
        if (!"order-service".equalsIgnoreCase(jwt.getClaimAsString("sub"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        productService.updateStock(dto.getProductId(), dto.getQuantity());
        return ResponseEntity.ok("Stock updated successfully");
    }

    @Operation(summary = "Check if product is available in required quantity")
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isProductAvailable(@PathVariable("id") Long id,
                                                      @RequestParam int quantity,
                                                      @AuthenticationPrincipal Jwt jwt) {
        boolean available = productService.isAvailable(id, quantity);
        return ResponseEntity.ok(available);
    }

    @Operation(summary = "Get featured products from categories")
    @GetMapping("/featured")
    public ResponseEntity<List<GetProductResponseDto>> getFeaturedProducts(@AuthenticationPrincipal Jwt jwt) {
        List<Product> products = productService.getFeaturedProducts();
        List<GetProductResponseDto> response = products.stream()
                .map(GetProductResponseDto::fromProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}