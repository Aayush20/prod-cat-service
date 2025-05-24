package org.example.prodcatservice.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ElasticSearchServiceImplTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private FailedIndexTaskRepository failedIndexTaskRepository;

    @InjectMocks
    private ElasticSearchServiceImpl elasticSearchService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIndexProductSuccess() throws IOException {
        Product product = getSampleProduct();

        doNothing().when(esClient).index(any(IndexRequest.class));

        elasticSearchService.indexProduct(product);

        verify(esClient).index(any(IndexRequest.class));
        verify(failedIndexTaskRepository, never()).save(any());
    }

    @Test
    void testIndexProductFailureFallbackToRepo() throws IOException {
        Product product = getSampleProduct();

        doThrow(new IOException("Elasticsearch down")).when(esClient).index(any(IndexRequest.class));

        elasticSearchService.indexProduct(product);

        verify(failedIndexTaskRepository).save(argThat(task ->
                task.getProductId().equals(product.getId()) &&
                        task.getReason().contains("Elasticsearch down") &&
                        task.getRetryCount() == 0
        ));
    }

    @Test
    void testDeleteProductFromIndex() throws IOException {
        doNothing().when(esClient).delete(ArgumentMatchers.<DeleteRequest>any());

        elasticSearchService.deleteProductFromIndex(123L);

        verify(esClient).delete(argThat((DeleteRequest req) ->
                req.index().equals("products") && req.id().equals("123")
        ));
    }

//    @Test
//    void testDynamicSearchReturnsPage() throws IOException {
//        // 1. Create the product doc that ES is expected to return
//        ProductDocument doc = new ProductDocument(1L, "Phone", "desc", 5000, "Electronics", 10, "SellerX", "img.jpg");
//
//        // 2. Mock Hit<ProductDocument> to wrap the above doc
//        Hit<ProductDocument> hit = mock(Hit.class);
//        when(hit.source()).thenReturn(doc);
//
//        // 3. Mock Hits<ProductDocument> to return list of hits
//        Hits<ProductDocument> hits = mock(Hits.class);
//        when(hits.hits()).thenReturn(List.of(hit));
//        when(hits.total()).thenReturn(null); // Optional if your code uses total
//
//        // 4. Mock SearchResponse<ProductDocument>
//        SearchResponse<ProductDocument> mockResponse = mock(SearchResponse.class);
//        when(mockResponse.hits()).thenReturn(hits);
//
//        // 5. Fix ambiguous method with explicit SearchRequest class
//        when(esClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(ProductDocument.class)))
//                .thenReturn(mockResponse);
//
//        // 6. Call the method and validate result
//        Page<ProductDocument> result = elasticSearchService.dynamicSearch(
//                "Phone", null, null, "Electronics", 0, 10, "title", SortOrder.Asc
//        );
//
//        assertThat(result).isNotNull();
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Phone");
//    }



    private Product getSampleProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Test Phone");
        product.setDescription("Awesome phone");
        product.setPrice(10000);
        product.setSeller("XYZ");
        product.setStock(5);
        product.setImageUrl("img.jpg");
        Category category = new Category();
        category.setName("Mobiles");
        product.setCategory(category);
        return product;
    }
}
