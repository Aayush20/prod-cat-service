// --- RollbackStockRequestDto.java ---
package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;

public class RollbackStockRequestDto {

    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Quantity to rollback", example = "3")
    private int quantity;

    @Schema(description = "Reason for rollback", example = "Order Cancelled")
    private String reason;

    public RollbackStockRequestDto() {}

    public RollbackStockRequestDto(Long productId, int quantity, String reason) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static RollbackStockRequestDtoBuilder builder() {
        return new RollbackStockRequestDtoBuilder();
    }

    public static class RollbackStockRequestDtoBuilder {
        private Long productId;
        private int quantity;
        private String reason;

        public RollbackStockRequestDtoBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public RollbackStockRequestDtoBuilder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public RollbackStockRequestDtoBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public RollbackStockRequestDto build() {
            return new RollbackStockRequestDto(productId, quantity, reason);
        }
    }
}

// --- Note on DTO Usage ---
// RollbackStockRequestDto is intentionally separate from UpdateStockRequestDto for clarity and flexibility:
// - UpdateStockRequestDto is used for order placement, reduces stock.
// - RollbackStockRequestDto is used for failure scenarios, restores stock.
// - Having separate DTOs allows cleaner Swagger docs, better auditing, and distinct validation logic.
