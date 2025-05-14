package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateStockRequestDto {
    @Schema(description = "Product ID", example = "101")
    private Long productId;

    @Schema(description = "Quantity to deduct", example = "2")
    private int quantity;

    @Schema(description = "Reason for stock update", example = "Order placed")
    private String reason;


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}
