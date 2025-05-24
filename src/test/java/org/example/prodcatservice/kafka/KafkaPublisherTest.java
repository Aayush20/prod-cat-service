package org.example.prodcatservice.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

class KafkaPublisherTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaPublisher = new KafkaPublisher(kafkaTemplate);

        // Inject the rollback retry topic manually (required due to @Value in component)
        ReflectionTestUtils.setField(kafkaPublisher, "rollbackRetryTopic", "product.stock.rollback.retry");
    }

    @Test
    void testPublishStockUpdatedEvent() {
        StockEvent event = new StockEvent("prod-123", 10, "UPDATED", "test", "user", System.currentTimeMillis());

        kafkaPublisher.publishStockUpdatedEvent(event);

        verify(kafkaTemplate).send("product.stock.updated", event);
    }

    @Test
    void testPublishStockRollbackEvent_success() {
        StockEvent event = new StockEvent("prod-456", 5, "ROLLBACK", "fail", "user", System.currentTimeMillis());

        kafkaPublisher.publishStockRollbackEvent(event);

        verify(kafkaTemplate).send("product.stock.rollback", event);
        verify(kafkaTemplate, never()).send(eq("product.stock.rollback.retry"), anyString(), any());
    }

    @Test
    void testPublishStockRollbackEvent_withFallback() {
        StockEvent event = new StockEvent("prod-999", 2, "ROLLBACK", "network issue", "user", System.currentTimeMillis());

        // Simulate primary Kafka failure
        doThrow(new RuntimeException("Kafka failure"))
                .when(kafkaTemplate).send("product.stock.rollback", event);

        kafkaPublisher.publishStockRollbackEvent(event);

        // Retry should happen with key & retry topic
        verify(kafkaTemplate).send("product.stock.rollback.retry", "prod-999", event);
    }
}
