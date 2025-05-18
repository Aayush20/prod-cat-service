package org.example.prodcatservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topic.rollback.retry:product.stock.rollback.retry}")
    private String rollbackRetryTopic;

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockUpdatedEvent(Object payload) {
        LOGGER.info("Publishing product.stock.updated event: {}", payload);
        kafkaTemplate.send("product.stock.updated", payload);
    }

    public void publishStockRollbackEvent(Object payload) {
        try {
            LOGGER.info("Publishing product.stock.rollback event: {}", payload);
            kafkaTemplate.send("product.stock.rollback", payload);
        } catch (Exception ex) {
            LOGGER.error("❌ Failed to publish stock.rollback event. Sending to retry topic.");
            try {
                kafkaTemplate.send(rollbackRetryTopic, ((StockEvent) payload).getProductId(), payload);
            } catch (Exception retryEx) {
                LOGGER.error("🚨 Retry topic also failed: {}", retryEx.getMessage());
                // DLQ handled by RollbackRetryConsumer
            }
        }

    }
}
