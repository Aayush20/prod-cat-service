package org.example.prodcatservice.dtos.product.responseDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.prodcatservice.models.Product;

public class UpdateProductResponseDto {

    @Schema(description = "ID of created product", example = "101")
    private Long id;

    @Schema(description = "Product title", example = "iPhone 15")
    private String title;

    @Schema(description = "Product description", example = "Latest iPhone model")
    private String description;

    @Schema(description = "Price of product", example = "1299.99")
    private double price;

    @Schema(description = "Success/Failure", example = "SUCCESS")
    private ResponseStatus status;

    @Schema(description = "Status message", example = "Product created successfully")
    private String message;

    @Schema(description = "Optional error code if failure", example = "1001")
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

    public static UpdateProductResponseDto fromProduct(Product product) {
        UpdateProductResponseDto responseDto = new UpdateProductResponseDto();
        responseDto.setId(product.getId());
        responseDto.setDescription(product.getDescription());
        responseDto.setTitle(product.getTitle());
        responseDto.setPrice(product.getPrice());


        return responseDto;
    }
}
