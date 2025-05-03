package org.example.prodcatservice.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.example.prodcatservice.models.FailedIndexTask;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.example.prodcatservice.repositories.FailedIndexTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ElasticSearchServiceImpl {

    private final ElasticsearchClient esClient;
    private final String indexName = "products";
    private final FailedIndexTaskRepository failedIndexTaskRepository;

    public ElasticSearchServiceImpl(ElasticsearchClient esClient,
                                    FailedIndexTaskRepository failedIndexTaskRepository) {
        this.esClient = esClient;
        this.failedIndexTaskRepository = failedIndexTaskRepository;
    }

    @Retryable(
            value = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void indexProduct(Product product) {
        ProductDocument doc = convertToDocument(product);
        IndexRequest<ProductDocument> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(product.getId().toString())
                .document(doc)
        );

        try {
            esClient.index(request);
            System.out.println("Indexed product " + product.getId());
        } catch (IOException e) {
            System.err.println("Elasticsearch indexing failed. Saving to retry queue. Reason: " + e.getMessage());
            FailedIndexTask task = new FailedIndexTask();
            task.setProductId(product.getId());
            task.setReason(e.getMessage());
            task.setRetryCount(0);
            task.setLastTriedAt(LocalDateTime.now());
            failedIndexTaskRepository.save(task);
        }
    }

    public void deleteProductFromIndex(Long productId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(indexName)
                    .id(productId.toString())
            );
            esClient.delete(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Page<ProductDocument> dynamicSearch(String query,
                                               Double minPrice,
                                               Double maxPrice,
                                               String category,
                                               int page,
                                               int size,
                                               String sortBy,
                                               SortOrder sortOrder) {
        try {
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            // Full-text search
            if (query != null && !query.isBlank()) {
                MultiMatchQuery matchQuery = MultiMatchQuery.of(m -> m
                        .query(query)
                        .fields("title", "description")
                );
                mustQueries.add(Query.of(q -> q.multiMatch(matchQuery)));
            }

            // Category filter
            if (category != null && !category.isBlank()) {
                filterQueries.add(Query.of(q -> q
                        .term(t -> t
                                .field("categoryName.keyword")
                                .value(category)
                        )
                ));
            }

            // Price filter temporarily removed
            // if (minPrice != null || maxPrice != null) {
            //     filterQueries.add(Query.of(q -> q.range(r -> {
            //         r.field("price");
            //         if (minPrice != null) r.gte(JsonData.of(minPrice));
            //         if (maxPrice != null) r.lte(JsonData.of(maxPrice));
            //         return r;
            //     })));
            // }

            // Final query
            Query finalQuery = Query.of(q -> q
                    .bool(b -> {
                        if (!mustQueries.isEmpty()) b.must(mustQueries);
                        if (!filterQueries.isEmpty()) b.filter(filterQueries);
                        return b;
                    })
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .from(page * size)
                    .size(size)
                    .sort(sort -> sort.field(f -> f.field(sortBy).order(sortOrder)))
                    .query(finalQuery)
            );

            SearchResponse<ProductDocument> response = esClient.search(searchRequest, ProductDocument.class);
            List<ProductDocument> products = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageImpl<>(products, PageRequest.of(page, size), totalHits);

        } catch (IOException e) {
            e.printStackTrace();
            return Page.empty();
        }
    }


    private ProductDocument convertToDocument(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        return new ProductDocument(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                categoryName,
                product.getStock(),
                product.getSeller(),
                product.getImageUrl()
        );
    }
}

