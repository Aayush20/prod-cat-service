package org.example.prodcatservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.prodcatservice.dtos.common.BaseResponse;
import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.PatchProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.UpdateStockRequestDto;
import org.example.prodcatservice.dtos.product.responseDtos.*;
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

    @Operation(
            summary = "Create a new product",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Product created successfully\", \"data\": { \"title\": \"iPhone 15\" } }")
                            })
                    )
            }
    )
    @PostMapping("/")
    public ResponseEntity<BaseResponse<CreateProductResponseDto>> createProduct(@RequestBody CreateProductRequestDto createProductRequestDto,
                                                                                @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is creating a product.", jwt.getSubject());
        Product product = createProductRequestDto.toProduct();
        Product saved = productService.createProduct(product);
        CreateProductResponseDto responseDto = CreateProductResponseDto.fromProduct(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success("Product created successfully", responseDto));
    }

    @Operation(
            summary = "Get product by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product fetched",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Product fetched successfully\", \"data\": { \"title\": \"MacBook Pro\" } }")
                            })
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GetProductResponseDto>> getProduct(@PathVariable("id") Long id,
                                                                          @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is fetching product with id {}", jwt.getSubject(), id);
        Product product = productService.getProduct(id);
        GetProductResponseDto response = GetProductResponseDto.fromProduct(product);
        return ResponseEntity.ok(BaseResponse.success("Product fetched successfully", response));
    }

    @Operation(
            summary = "Delete product by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product deleted",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Product deleted successfully\", \"data\": \"Deleted\" }")
                            })
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> deleteProduct(@PathVariable("id") Long id,
                                                              @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is deleting product with id {}", jwt.getSubject(), id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(BaseResponse.success("Product deleted successfully", "Deleted"));
    }

    @Operation(
            summary = "Partially update a product",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Product updated successfully\", \"data\": { \"title\": \"Updated Product\" } }")
                            })
                    )
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<PatchProductResponseDto>> partialUpdateProduct(@PathVariable("id") Long id,
                                                                                      @RequestBody PatchProductRequestDto patchProductRequestDto,
                                                                                      @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} is updating product with id {}", jwt.getSubject(), id);
        Product updated = productService.partialUpdateProduct(id, patchProductRequestDto.toProduct());
        PatchProductResponseDto response = PatchProductResponseDto.fromProduct(updated);
        return ResponseEntity.ok(BaseResponse.success("Product updated successfully", response));
    }

    @Operation(
            summary = "Update product stock (for order-service only)",
            description = "Only callable by order-service microservice via JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock updated",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Stock updated successfully\", \"data\": \"OK\" }")
                            })
                    ),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access")
            }
    )
    @PatchMapping("/update-stock")
    public ResponseEntity<BaseResponse<String>> updateStock(@RequestBody UpdateStockRequestDto dto,
                                                            @AuthenticationPrincipal Jwt jwt) {
        if (!"order-service".equalsIgnoreCase(jwt.getClaimAsString("sub"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(BaseResponse.failure("Access denied"));
        }
        productService.updateStock(dto.getProductId(), dto.getQuantity(), jwt.getSubject(), dto.getReason());
        return ResponseEntity.ok(BaseResponse.success("Stock updated successfully", "OK"));
    }

    @Operation(
            summary = "Check if product is available in required quantity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Availability status",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Available", value = "{ \"status\": \"SUCCESS\", \"message\": \"Availability checked\", \"data\": true }")
                            })
                    )
            }
    )
    @GetMapping("/{id}/available")
    public ResponseEntity<BaseResponse<Boolean>> isProductAvailable(@PathVariable("id") Long id,
                                                                    @RequestParam int quantity,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        boolean available = productService.isAvailable(id, quantity);
        return ResponseEntity.ok(BaseResponse.success("Availability checked", available));
    }

    @Operation(
            summary = "Get featured products from categories",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of featured products",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Featured products fetched\", \"data\": [ { \"title\": \"AirPods\" } ] }")
                            })
                    )
            }
    )
    @GetMapping("/featured")
    public ResponseEntity<BaseResponse<List<GetProductResponseDto>>> getFeaturedProducts(@AuthenticationPrincipal Jwt jwt) {
        List<Product> products = productService.getFeaturedProducts();
        List<GetProductResponseDto> response = products.stream()
                .map(GetProductResponseDto::fromProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(BaseResponse.success("Featured products fetched", response));
    }
}
