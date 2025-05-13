package org.example.prodcatservice.jobs;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.prodcatservice.models.FailedIndexTask;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.models.RetryStatus;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.example.prodcatservice.services.ElasticSearchServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class IndexRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(IndexRetryScheduler.class);
    private final FailedIndexTaskRepository failedIndexTaskRepository;
    private final ProductRepository productRepository;
    private final ElasticSearchServiceImpl elasticSearchService;
    private final MeterRegistry meterRegistry;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    public IndexRetryScheduler(FailedIndexTaskRepository failedIndexTaskRepository,
                               ProductRepository productRepository,
                               ElasticSearchServiceImpl elasticSearchService,
                               MeterRegistry meterRegistry) {
        this.failedIndexTaskRepository = failedIndexTaskRepository;
        this.productRepository = productRepository;
        this.elasticSearchService = elasticSearchService;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelay = 30000)
    public void retryFailedIndexTasks() {
        List<FailedIndexTask> failedTasks = failedIndexTaskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus() != RetryStatus.GIVEN_UP)
                .toList();

        for (FailedIndexTask task : failedTasks) {
            if (task.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                task.setStatus(RetryStatus.GIVEN_UP);
                failedIndexTaskRepository.save(task);
                log.warn("Giving up on product {} after {} retries", task.getProductId(), task.getRetryCount());
                continue;
            }

            try {
                task.setStatus(RetryStatus.RETRYING);
                failedIndexTaskRepository.save(task);

                Product product = productRepository.findById(task.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found for retry: " + task.getProductId()));

                elasticSearchService.indexProduct(product);
                meterRegistry.counter("elasticsearch.retry.success").increment();
                failedIndexTaskRepository.delete(task);
                log.info("Retry succeeded for product {}", task.getProductId());

            } catch (Exception e) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setReason(e.getMessage());
                task.setLastTriedAt(LocalDateTime.now());
                task.setStatus(RetryStatus.PENDING);
                failedIndexTaskRepository.save(task);
                meterRegistry.counter("elasticsearch.retry.failure").increment();
                log.warn("Retry failed for product {} (attempt {}): {}", task.getProductId(), task.getRetryCount(), e.getMessage());
            }
        }
    }

}
