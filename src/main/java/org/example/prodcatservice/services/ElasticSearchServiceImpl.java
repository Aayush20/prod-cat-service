package org.example.prodcatservice.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticSearchServiceImpl {

    private final ElasticsearchClient esClient;
    private final String indexName = "products";

    public ElasticSearchServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * Index a product into Elasticsearch.
     */
    public void indexProduct(Product product) {
        // Convert the Product entity into the search document.
        ProductDocument doc = convertToDocument(product);
        try {
            IndexRequest<ProductDocument> request = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(product.getId().toString()) // Convert the Long ID to String
                    .document(doc)
            );
            IndexResponse response = esClient.index(request);
            System.out.println("Indexed product " + product.getId() + " with result: " + response.result().name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a product document from the Elasticsearch index.
     */
    public void deleteProductFromIndex(Long productId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(indexName)
                    .id(productId.toString()) // Convert the Long ID to String
            );
            esClient.delete(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple search. Returns 10 products that match the query, sorted by title.
     */
    public List<ProductDocument> simpleSearch(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title", "description")
                            )
                    )
                    .sort(so -> so.field(f -> f.field("title").order(SortOrder.Asc)))
                    .size(10)
            );
            SearchResponse<ProductDocument> response = esClient.search(request, ProductDocument.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Filtered search with pagination and sorting.
     *
     * @param query     The query string to search in title and description.
     * @param minPrice  Optional minimum price filter.
     * @param maxPrice  Optional maximum price filter.
     * @param page      Page number (0-based index).
     * @param size      Number of products per page.
     * @param sortBy    The field to sort by.
     * @param sortOrder Sort order (ASC or DESC).
     * @return Page of matched products.
     */
    public Page<ProductDocument> filteredSearch(String query, Double minPrice, Double maxPrice,
                                                int page, int size, String sortBy, SortOrder sortOrder) {
        try {
            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                    .index(indexName)
                    .from(page * size)
                    .size(size)
                    .sort(s -> s.field(f -> f
                            .field(sortBy)
                            .order(sortOrder)
                    ));

            // Build a bool query with a must clause for the text search and a filter clause for price range.
            searchRequestBuilder.query(q -> q
                    .bool(b -> {
                        b.must(m -> m
                                .multiMatch(mm -> mm
                                        .query(query)
                                        .fields("title", "description")
                                )
                        );





                        return b;
                    })
            );

            SearchRequest request = searchRequestBuilder.build();
            SearchResponse<ProductDocument> response = esClient.search(request, ProductDocument.class);

            // Retrieve total hits count.
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

            // Collect the search results.
            List<ProductDocument> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            return new PageImpl<>(results, PageRequest.of(page, size), totalHits);
        } catch (IOException e) {
            e.printStackTrace();
            return Page.empty();
        }
    }

    /**
     * Helper: Convert a Product into a ProductDocument.
     */
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
