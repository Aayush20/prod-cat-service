package org.example.prodcatservice.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ElasticSearchServiceImplTest {

    @Test
    void testIndexProduct_failure_savesToRetryQueue() throws Exception {
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        FailedIndexTaskRepository retryRepo = mock(FailedIndexTaskRepository.class);
        ElasticSearchServiceImpl service = new ElasticSearchServiceImpl(client, retryRepo);
        Product p = new Product();
        p.setId(1L);
        doThrow(new RuntimeException("error")).when(client).index((IndexRequest<Object>) any());
        service.indexProduct(p);
        verify(retryRepo, times(1)).save(any());
    }
}
