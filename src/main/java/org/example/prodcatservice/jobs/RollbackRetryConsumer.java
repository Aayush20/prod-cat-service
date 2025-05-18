package org.example.prodcatservice.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.prodcatservice.kafka.KafkaPublisher;
import org.example.prodcatservice.kafka.StockEvent;
import org.example.prodcatservice.models.RetryDeadLetterLog;
import org.example.prodcatservice.repositories.RetryDeadLetterLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RollbackRetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(RollbackRetryConsumer.class);
    private final KafkaPublisher kafkaPublisher;
    private final RetryDeadLetterLogRepository deadLetterRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public RollbackRetryConsumer(KafkaPublisher kafkaPublisher, RetryDeadLetterLogRepository deadLetterRepo) {
        this.kafkaPublisher = kafkaPublisher;
        this.deadLetterRepo = deadLetterRepo;
    }

    @KafkaListener(topics = "${topic.rollback.retry:product.stock.rollback.retry}", groupId = "prod-cat-retry-group")
    public void retryFailedRollback(ConsumerRecord<String, Object> record) {
        String productId = record.key();
        Object value = record.value();

        try {
            if (value instanceof StockEvent event) {
                log.info("🔁 Retrying stock rollback for productId={} reason={}", event.getProductId(), event.getReason());
                kafkaPublisher.publishStockRollbackEvent(event);
            } else {
                log.warn("⚠️ Unknown event class in retry topic: {}", value.getClass().getName());
            }
        } catch (Exception ex) {
            log.error("🔥 Retry failed again for productId={} to rollback: {}", productId, ex.getMessage());

            RetryDeadLetterLog logEntity = new RetryDeadLetterLog();
            logEntity.setTopic(record.topic());
            logEntity.setKey(productId);
            logEntity.setPayload(value.toString());
            logEntity.setErrorMessage(ex.getMessage());
            logEntity.setCreatedAt(LocalDateTime.now());
            deadLetterRepo.save(logEntity);
        }
    }
}
