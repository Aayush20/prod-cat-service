package org.example.prodcatservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.prodcatservice.dtos.common.BaseResponse;
import org.example.prodcatservice.dtos.product.requestDtos.CreateProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.PatchProductRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.RollbackStockRequestDto;
import org.example.prodcatservice.dtos.product.requestDtos.UpdateStockRequestDto;
import org.example.prodcatservice.dtos.product.responseDtos.*;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.services.ProductService;
import org.example.prodcatservice.services.TokenService;
import org.example.prodcatservice.utils.TokenClaimUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/products")
@Tag(name = "Product APIs")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final CacheManager cacheManager;
    private final TokenService tokenService;

    public ProductController(ProductService productService, CacheManager cacheManager,
                             TokenService tokenService) {
        this.productService = productService;
        this.cacheManager = cacheManager;
        this.tokenService = tokenService;
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
                                                                                @RequestHeader("Authorization") String tokenHeader) {

//        if (!hasRole(jwt, "ADMIN")) {
//            return ResponseEntity.status(403).body(BaseResponse.failure("Only admin can create products."));
//        }
        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Only admin can create products."));
        }
        log.info("User {} is creating a product.", token.getSub());
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
                                                              @RequestHeader("Authorization") String tokenHeader) {

        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Only admin can delete products."));
        }
        log.info("User {} is deleting product with id {}", token.getSub(), id);
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
                                                                                      @RequestHeader("Authorization") String tokenHeader) {

        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Only admin can update products."));
        }

        log.info("User {} is updating product with id {}", token.getSub(), id);
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
                                                            @RequestHeader("Authorization") String tokenHeader) {
        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.isSystemCall(token, "order-service")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Access denied"));
        }
        productService.updateStock(dto.getProductId(), dto.getQuantity(), token.getSub(), dto.getReason());
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


    @Operation(
            summary = "Rollback stock on payment/order failure",
            description = "This is an internal endpoint to rollback stock due to order cancel or payment failure",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock rolled back successfully",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = "{ \"status\": \"SUCCESS\", \"message\": \"Stock rollback successful\", \"data\": null }")))
            }
    )
    @PostMapping("/internal/rollback-stock")
    @PreAuthorize("hasAuthority('SCOPE_internal') or hasAuthority('ROLE_ORDER_SERVICE')")
    public ResponseEntity<BaseResponse<Void>> rollbackStock(@RequestBody @Valid RollbackStockRequestDto dto,
                                                            @RequestHeader("Authorization") String tokenHeader) {

        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasScope(token, "internal") && !TokenClaimUtils.hasRole(token, "ORDER_SERVICE")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Access denied"));
        }
        productService.rollbackStock(dto);
        return ResponseEntity.ok(BaseResponse.success("Stock rollback successful", null));
    }

    @Operation(summary = "Clear product cache manually (admin only)")
    @ApiResponse(responseCode = "200", description = "Cache cleared")
    @DeleteMapping("/internal/cache/clear")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> clearProductCache(
            @RequestHeader("Authorization") String tokenHeader) {
        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Only admin can clear cache."));
        }
        if (cacheManager.getCache("products") != null) {
            cacheManager.getCache("products").clear(); // Clears all product entries
        }
        return ResponseEntity.ok(BaseResponse.success("Product cache cleared", "OK"));
    }

//    @GetMapping("/me")
//    public ResponseEntity<?> getJwtClaims(@AuthenticationPrincipal Jwt jwt) {
//        return ResponseEntity.ok(jwt.getClaims());
//    }

    @Operation(summary = "Get current token claims (debug only)")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentTokenInfo(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(tokenService.introspect(tokenHeader));
    }


}
