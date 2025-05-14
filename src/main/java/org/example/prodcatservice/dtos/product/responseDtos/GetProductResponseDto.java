package org.example.prodcatservice.dtos.product.responseDtos;

import org.example.prodcatservice.models.Product;
import io.swagger.v3.oas.annotations.media.Schema;

public class GetProductResponseDto {

    @Schema(description = "Product ID", example = "101")
    private Long id;

    @Schema(description = "Product title", example = "MacBook Pro")
    private String title;

    @Schema(description = "Product description", example = "High-performance Apple laptop")
    private String description;

    @Schema(description = "Product price", example = "2399.00")
    private double price;

    @Schema(description = "Image URL", example = "https://cdn.example.com/macbook.png")
    private String imageUrl;

    @Schema(description = "Seller name", example = "Apple Store")
    private String seller;

    @Schema(description = "Available stock", example = "12")
    private int stock;

    @Schema(description = "Category name", example = "electronics")
    private String categoryName;

    @Schema(description = "Response status", example = "SUCCESS")
    private ResponseStatus status;

    @Schema(description = "Response message", example = "Product fetched successfully")
    private String message;

    @Schema(description = "Error code if any", example = "1004")
    private Long errorCode;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static GetProductResponseDto fromProduct(Product product) {
        GetProductResponseDto responseDto = new GetProductResponseDto();
        responseDto.setId(product.getId());
        responseDto.setDescription(product.getDescription());
        responseDto.setTitle(product.getTitle());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        responseDto.setStock(product.getStock());
        responseDto.setSeller(product.getSeller());
        responseDto.setImageUrl(product.getImageUrl());
        return responseDto;
    }
}
