package org.example.prodcatservice.controllers;

import org.example.prodcatservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RedisCacheStatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RedisCacheStatsController controller =
                new RedisCacheStatsController(cacheManager, redisTemplate, tokenService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetCacheStats() throws Exception {
        Set<String> mockKeys = Set.of("prod:1", "cat:3");
        when(redisTemplate.keys("*")).thenReturn(mockKeys);
        when(cacheManager.getCacheNames()).thenReturn(Set.of("productCache", "categoryCache"));

        mockMvc.perform(get("/admin/cache-stats")
                        .header("Authorization", "Bearer mockToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyCount").value(2))
                .andExpect(jsonPath("$.cacheNames").isArray())
                .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    void testGetCacheStatsWithEmptyKeys() throws Exception {
        when(redisTemplate.keys("*")).thenReturn(Set.of());
        when(cacheManager.getCacheNames()).thenReturn(Set.of());

        mockMvc.perform(get("/admin/cache-stats")
                        .header("Authorization", "Bearer mockToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyCount").value(0))
                .andExpect(jsonPath("$.cacheNames").isArray())
                .andExpect(jsonPath("$.keys").isArray());
    }
}
