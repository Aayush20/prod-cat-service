package org.example.prodcatservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class FailedIndexTask extends BaseModel {

    private Long productId;

    private String reason;

    @Column(nullable = false)
    private int retryCount = 0;

    private LocalDateTime lastTriedAt = LocalDateTime.now();

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastTriedAt() {
        return lastTriedAt;
    }

    public void setLastTriedAt(LocalDateTime lastTriedAt) {
        this.lastTriedAt = lastTriedAt;
    }
}
