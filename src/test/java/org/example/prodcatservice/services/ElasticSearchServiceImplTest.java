//package org.example.prodcatservice.services;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import org.example.prodcatservice.models.Product;
//import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
//import org.junit.jupiter.api.Test;
//import java.io.IOException;
//
//import static org.mockito.Mockito.*;
//
//public class ElasticSearchServiceImplTest {
//
//    @Test
//    void testIndexProduct_savesToRetryQueueOnFailure() throws Exception {
//        // Mocks
//        ElasticsearchClient esClient = mock(ElasticsearchClient.class);
//        FailedIndexTaskRepository failedRepo = mock(FailedIndexTaskRepository.class);
//
//        ElasticSearchServiceImpl service = new ElasticSearchServiceImpl(esClient, failedRepo);
//
//        // Sample product
//        Product p = new Product();
//        p.setId(1L);
//        p.setTitle("Phone");
//
//        // Force indexing to throw an exception
//        doThrow(new RuntimeException("ES failed")).when(esClient).index(any());
//
//        // Act
//        service.indexProduct(p);
//
//        // Assert: retry queue should be called
//        verify(failedRepo, times(1)).save(any());
//    }
//}
