package org.example.prodcatservice.controllers;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/cache-stats")
public class RedisCacheStatsController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheStatsController(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<String, Object> getCacheStats() {
        Set<String> keys = redisTemplate.keys("*");
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheNames", cacheManager.getCacheNames());
        stats.put("keyCount", keys != null ? keys.size() : 0);
        stats.put("keys", keys);
        return stats;
    }
}
