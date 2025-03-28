package org.example.prodcatservice.controllers;

import co.elastic.clients.elasticsearch._types.SortOrder;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.example.prodcatservice.services.ElasticSearchServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final ElasticSearchServiceImpl elasticSearchService;

    public SearchController(ElasticSearchServiceImpl elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    /**
     * Simple search endpoint: returns 10 products that match the query, sorted by title.
     */
    @GetMapping("/simple")
    public Page<ProductDocument> simpleSearch(@RequestParam String query,
                                              @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User " + jwt.getSubject() + " is performing a simple search.");
        return elasticSearchService.filteredSearch(query, null, null, 0, 10, "title", SortOrder.Asc);
    }

    /**
     * Advanced search endpoint with filters, pagination, and sorting.
     */
    @GetMapping("/advanced")
    public Page<ProductDocument> advancedSearch(
            @RequestParam String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            @AuthenticationPrincipal Jwt jwt) {

        System.out.println("User " + jwt.getSubject() + " is performing an advanced search.");
        SortOrder order = sortOrder.equalsIgnoreCase("ASC") ? SortOrder.Asc : SortOrder.Desc;
        return elasticSearchService.filteredSearch(query, minPrice, maxPrice, page, size, sortBy, order);
    }
}
