package org.example.prodcatservice.controllers;

import co.elastic.clients.elasticsearch._types.SortOrder;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.example.prodcatservice.models.elasticdocs.ProductDocument;
import org.example.prodcatservice.services.ElasticSearchServiceImpl;
import org.example.prodcatservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import io.micrometer.core.instrument.Counter;

class SearchControllerTest {

    @Mock
    private ElasticSearchServiceImpl elasticSearchService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private TokenService tokenService;

    private MockMvc mockMvc;

    @Mock
    private Counter searchCounter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Correct way to mock a method that returns a Counter
        when(meterRegistry.counter("products.search.count")).thenReturn(searchCounter);

        // Correct way to handle void method
        doNothing().when(searchCounter).increment();

        SearchController controller = new SearchController(elasticSearchService, meterRegistry, tokenService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testSearchProductsSuccess() throws Exception {
        // Mock token introspection
        TokenIntrospectionResponseDTO mockToken = new TokenIntrospectionResponseDTO();
        mockToken.setSub("user123");
        when(tokenService.introspect(anyString())).thenReturn(mockToken);

        // Mock search result
        ProductDocument doc = new ProductDocument(1L, "iPhone", "flagship", 999.0,
                "Mobiles", 10, "Apple", "img.jpg");
        Page<ProductDocument> mockPage = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
        when(elasticSearchService.dynamicSearch(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/search")
                        .param("query", "iphone")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500")
                        .param("category", "Mobiles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "price")
                        .param("sortOrder", "ASC")
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Search completed"))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void testSearchProductsEmptyResult() throws Exception {
        when(tokenService.introspect(anyString())).thenReturn(new TokenIntrospectionResponseDTO());
        Page<ProductDocument> emptyPage = Page.empty(PageRequest.of(0, 10));
        when(elasticSearchService.dynamicSearch(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/search")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }
}
