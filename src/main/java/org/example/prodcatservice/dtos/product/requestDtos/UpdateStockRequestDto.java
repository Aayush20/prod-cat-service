package org.example.prodcatservice.dtos.product.requestDtos;

import lombok.Data;

@Data
public class UpdateStockRequestDto {
    private Long productId;
    private int quantity;
    private String reason;// Quantity to deduct

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
