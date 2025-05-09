package org.example.prodcatservice.controllers;

import co.elastic.clients.elasticsearch._types.SortOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.example.prodcatservice.services.ElasticSearchServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@Tag(name = "Search APIs")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final ElasticSearchServiceImpl elasticSearchService;

    public SearchController(ElasticSearchServiceImpl elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @Operation(summary = "Dynamic search endpoint with filters, pagination, sorting")
    @GetMapping
    public Page<ProductDocument> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("User {} is performing a dynamic search with query: {}", jwt.getSubject(), query);
        SortOrder order = sortOrder.equalsIgnoreCase("ASC") ? SortOrder.Asc : SortOrder.Desc;

        return elasticSearchService.dynamicSearch(query, minPrice, maxPrice, category, page, size, sortBy, order);
    }
}
