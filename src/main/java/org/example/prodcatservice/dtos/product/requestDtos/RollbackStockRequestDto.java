package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request for rolling back product stock (batch)")
public class RollbackStockRequestDto {

    @NotEmpty
    @Schema(description = "List of products to rollback")
    private List<ProductRollbackEntry> products;

    @Schema(description = "Reason for rollback", example = "Order cancelled or payment failed")
    private String reason;

    public List<ProductRollbackEntry> getProducts() {
        return products;
    }

    public void setProducts(List<ProductRollbackEntry> products) {
        this.products = products;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static class ProductRollbackEntry {
        @Schema(description = "Product ID", example = "1")
        private Long productId;

        @Schema(description = "Quantity to rollback", example = "3")
        private int quantity;

        public ProductRollbackEntry() {}

        public ProductRollbackEntry(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

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
}
