package org.example.prodcatservice.jobs;

import org.example.prodcatservice.models.FailedIndexTask;
import org.example.prodcatservice.models.Product;
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

    private static final int MAX_RETRY_ATTEMPTS = 3;

    public IndexRetryScheduler(FailedIndexTaskRepository failedIndexTaskRepository,
                               ProductRepository productRepository,
                               ElasticSearchServiceImpl elasticSearchService) {
        this.failedIndexTaskRepository = failedIndexTaskRepository;
        this.productRepository = productRepository;
        this.elasticSearchService = elasticSearchService;
    }

    @Scheduled(fixedDelay = 30000)
    public void retryFailedIndexTasks() {
        List<FailedIndexTask> failedTasks = failedIndexTaskRepository.findAll();
        for (FailedIndexTask task : failedTasks) {
            if (task.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                log.warn("Skipping retry for product {} after {} attempts", task.getProductId(), task.getRetryCount());
                continue;
            }

            try {
                Product product = productRepository.findById(task.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found for retry: " + task.getProductId()));

                elasticSearchService.indexProduct(product);
                failedIndexTaskRepository.delete(task);
                log.info("Retry succeeded for product {}", task.getProductId());

            } catch (Exception e) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setReason(e.getMessage());
                task.setLastTriedAt(LocalDateTime.now());
                failedIndexTaskRepository.save(task);
                log.warn("Retry failed for product {} (attempt {}): {}", task.getProductId(), task.getRetryCount(), e.getMessage());
            }
        }
    }
}
