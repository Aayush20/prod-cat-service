package org.example.prodcatservice.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.example.prodcatservice.security.AdminOnly;
import org.example.prodcatservice.services.TokenService;
import org.example.prodcatservice.utils.TokenClaimUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/cache-stats")
public class RedisCacheStatsController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenService tokenService;

    public RedisCacheStatsController(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate, TokenService tokenService) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.tokenService = tokenService;
    }

    @GetMapping
    @AdminOnly
    public Map<String, Object> getCacheStats(@RequestHeader("Authorization") String tokenHeader) {
        Set<String> keys = redisTemplate.keys("*");
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheNames", cacheManager.getCacheNames());
        stats.put("keyCount", keys != null ? keys.size() : 0);
        stats.put("keys", keys);
        return stats;
    }
}
