package org.example.prodcatservice.jobs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.prodcatservice.models.FailedIndexTask;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.models.RetryStatus;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.example.prodcatservice.repositories.ProductRepository;
import org.example.prodcatservice.services.ElasticSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class IndexRetrySchedulerTest {

    @Mock
    private FailedIndexTaskRepository failedIndexTaskRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ElasticSearchServiceImpl elasticSearchService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter successCounter;

    @Mock
    private Counter failureCounter;

    @InjectMocks
    private IndexRetryScheduler indexRetryScheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("elasticsearch.retry.success")).thenReturn(successCounter);
        when(meterRegistry.counter("elasticsearch.retry.failure")).thenReturn(failureCounter);
    }

    @Test
    void testRetrySuccessAndTaskDeleted() {
        FailedIndexTask task = createTask(1L, 1, RetryStatus.PENDING);
        Product product = new Product();
        product.setId(1L);

        when(failedIndexTaskRepository.findAll()).thenReturn(List.of(task));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        indexRetryScheduler.retryFailedIndexTasks();

        verify(elasticSearchService).indexProduct(product);
        verify(successCounter).increment();
        verify(failedIndexTaskRepository).delete(task);
    }

    @Test
    void testRetryFailsIncrementsCount() {
        FailedIndexTask task = createTask(2L, 1, RetryStatus.PENDING);

        when(failedIndexTaskRepository.findAll()).thenReturn(List.of(task));
        when(productRepository.findById(2L)).thenThrow(new RuntimeException("Product not found"));

        indexRetryScheduler.retryFailedIndexTasks();

        verify(failureCounter).increment();

        ArgumentCaptor<FailedIndexTask> captor = ArgumentCaptor.forClass(FailedIndexTask.class);
        verify(failedIndexTaskRepository, times(2)).save(captor.capture());

        List<FailedIndexTask> savedCalls = captor.getAllValues();

        // 1st save = RETRYING status
        assertThat(savedCalls.get(0).getStatus()).isEqualTo(RetryStatus.RETRYING);

        // 2nd save = updated retryCount and reverted status
        FailedIndexTask finalSavedTask = savedCalls.get(1);
        assertThat(finalSavedTask.getProductId()).isEqualTo(2L);
        assertThat(finalSavedTask.getRetryCount()).isEqualTo(2);
        assertThat(finalSavedTask.getStatus()).isEqualTo(RetryStatus.PENDING);
    }



    @Test
    void testExceedsMaxRetryMarksGivenUp() {
        FailedIndexTask task = createTask(3L, 3, RetryStatus.PENDING);

        when(failedIndexTaskRepository.findAll()).thenReturn(List.of(task));

        indexRetryScheduler.retryFailedIndexTasks();

        verify(failedIndexTaskRepository).save(argThat(t ->
                t.getProductId().equals(3L) &&
                        t.getStatus() == RetryStatus.GIVEN_UP
        ));
    }

    private FailedIndexTask createTask(Long productId, int retryCount, RetryStatus status) {
        FailedIndexTask task = new FailedIndexTask();
        task.setProductId(productId);
        task.setRetryCount(retryCount);
        task.setStatus(status);
        return task;
    }
}
