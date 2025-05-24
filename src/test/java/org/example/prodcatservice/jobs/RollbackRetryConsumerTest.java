package org.example.prodcatservice.jobs;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.prodcatservice.kafka.KafkaPublisher;
import org.example.prodcatservice.kafka.StockEvent;
import org.example.prodcatservice.models.RetryDeadLetterLog;
import org.example.prodcatservice.repositories.RetryDeadLetterLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class RollbackRetryConsumerTest {

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private RetryDeadLetterLogRepository deadLetterRepo;

    @InjectMocks
    private RollbackRetryConsumer rollbackRetryConsumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuccessfulRetryWithStockEvent() {
        StockEvent event = new StockEvent("prod-123", 10, "ROLLBACK", "Insufficient stock", "order-service", System.currentTimeMillis());
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("product.stock.rollback.retry", 0, 0L, event.getProductId(), event);

        rollbackRetryConsumer.retryFailedRollback(record);

        verify(kafkaPublisher).publishStockRollbackEvent(event);
        verifyNoInteractions(deadLetterRepo);
    }

    @Test
    void testUnknownPayloadTypeLogsWarning() {
        Object invalidPayload = new Object(); // Not a StockEvent
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("product.stock.rollback.retry", 0, 0L, "prod-456", invalidPayload);

        rollbackRetryConsumer.retryFailedRollback(record);

        verifyNoInteractions(kafkaPublisher);
        verifyNoInteractions(deadLetterRepo);
    }

    @Test
    void testRetryFailureIsLoggedToDeadLetterRepo() {
        StockEvent event = new StockEvent("prod-999", 5, "ROLLBACK", "Kafka issue", "order-service", System.currentTimeMillis());
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("product.stock.rollback.retry", 0, 0L, event.getProductId(), event);

        doThrow(new RuntimeException("Kafka send failure")).when(kafkaPublisher).publishStockRollbackEvent(event);

        rollbackRetryConsumer.retryFailedRollback(record);

        ArgumentCaptor<RetryDeadLetterLog> captor = ArgumentCaptor.forClass(RetryDeadLetterLog.class);
        verify(deadLetterRepo).save(captor.capture());

        RetryDeadLetterLog savedLog = captor.getValue();

        assertThat(savedLog.getKey()).isEqualTo("prod-999");
        assertThat(savedLog.getTopic()).isEqualTo("product.stock.rollback.retry");
        assertThat(savedLog.getErrorMessage()).isEqualTo("Kafka send failure");
        assertThat(savedLog.getPayload()).contains("ROLLBACK"); // safer than "Kafka"
        assertThat(savedLog.getCreatedAt()).isNotNull();
    }

}
