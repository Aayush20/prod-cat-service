package org.example.prodcatservice.dtos.product.responseDtos;

import java.time.Instant;

public class InventoryLogResponseDto {
    private Long id;
    private Long productId;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String updatedBy;
    private String reason;
    private Instant timestamp;

    public InventoryLogResponseDto() {
    }

    public InventoryLogResponseDto(Long id, Long productId, Integer previousQuantity,
                                   Integer newQuantity, String updatedBy, String reason, Instant timestamp) {
        this.id = id;
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.updatedBy = updatedBy;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long productId;
        private Integer previousQuantity;
        private Integer newQuantity;
        private String updatedBy;
        private String reason;
        private Instant timestamp;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder previousQuantity(Integer previousQuantity) {
            this.previousQuantity = previousQuantity;
            return this;
        }

        public Builder newQuantity(Integer newQuantity) {
            this.newQuantity = newQuantity;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public InventoryLogResponseDto build() {
            return new InventoryLogResponseDto(id, productId, previousQuantity, newQuantity, updatedBy, reason, timestamp);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(Integer previousQuantity) { this.previousQuantity = previousQuantity; }

    public Integer getNewQuantity() { return newQuantity; }
    public void setNewQuantity(Integer newQuantity) { this.newQuantity = newQuantity; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
