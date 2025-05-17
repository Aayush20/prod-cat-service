package org.example.prodcatservice.kafka;

public class StockEvent {
    private String productId;
    private int quantity;
    private String eventType; // "UPDATED" or "ROLLBACK"
    private String reason;
    private String triggeredBy;
    private long timestamp;

    public StockEvent() {}

    public StockEvent(String productId, int quantity, String eventType, String reason, String triggeredBy, long timestamp) {
        this.productId = productId;
        this.quantity = quantity;
        this.eventType = eventType;
        this.reason = reason;
        this.triggeredBy = triggeredBy;
        this.timestamp = timestamp;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
