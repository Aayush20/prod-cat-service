package org.example.prodcatservice.dtos.product.requestDtos;

import lombok.Data;

@Data
public class UpdateStockRequestDto {
    private Long productId;
    private int quantity;  // Quantity to deduct

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
}
