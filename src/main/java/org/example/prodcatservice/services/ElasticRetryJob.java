package org.example.prodcatservice.services;

import org.example.prodcatservice.models.FailedIndexTask;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticRetryJob {

    private final FailedIndexTaskRepository retryRepo;
    private final ProductRepository productRepo;
    private final ElasticSearchServiceImpl elasticSearchService;

    public ElasticRetryJob(FailedIndexTaskRepository retryRepo,
                           ProductRepository productRepo,
                           ElasticSearchServiceImpl elasticSearchService) {
        this.retryRepo = retryRepo;
        this.productRepo = productRepo;
        this.elasticSearchService = elasticSearchService;
    }

    // Retry every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void retryFailedTasks() {
        List<FailedIndexTask> tasks = retryRepo.findTop10ByOrderByRetryCountAscLastTriedAtAsc();

        for (FailedIndexTask task : tasks) {
            try {
                Product product = productRepo.findById(task.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + task.getProductId()));

                elasticSearchService.indexProduct(product);
                retryRepo.delete(task); // Remove from retry queue after success

            } catch (Exception e) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setLastTriedAt(java.time.LocalDateTime.now());
                task.setReason(e.getMessage());
                retryRepo.save(task);
            }
        }
    }
}
