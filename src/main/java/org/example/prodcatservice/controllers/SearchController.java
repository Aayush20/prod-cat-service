package org.example.prodcatservice.controllers;

import co.elastic.clients.elasticsearch._types.SortOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.prodcatservice.dtos.common.BaseResponse;
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

    @Operation(
            summary = "Dynamic search with filters, pagination, sorting",
            description = "Search products using full-text query, price range, category, pagination, and sorting",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search results returned successfully",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Search completed\", \"data\": [ { \"id\": 1, \"title\": \"iPhone 15\", \"price\": 1200 } ] }")
                            })
                    )
            }
    )
    @GetMapping
    public BaseResponse<Page<ProductDocument>> searchProducts(
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

        Page<ProductDocument> results = elasticSearchService.dynamicSearch(query, minPrice, maxPrice, category, page, size, sortBy, order);
        return BaseResponse.success("Search completed", results);
    }
}
