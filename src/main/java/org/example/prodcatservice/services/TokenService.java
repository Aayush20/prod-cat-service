package org.example.prodcatservice.services;

import org.example.prodcatservice.clients.AuthClient;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final String CACHE_KEY_PREFIX = "token:introspect:";
    private static final long TTL_SECONDS = 300;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public TokenIntrospectionResponseDTO introspect(String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        String cacheKey = CACHE_KEY_PREFIX + token;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof TokenIntrospectionResponseDTO dto) {
            logger.info("✅ Redis cache hit for token");
            return dto;
        }

        TokenIntrospectionResponseDTO result = authClient.validateToken(tokenHeader);
        redisTemplate.opsForValue().set(cacheKey, result, TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("📦 Cached token introspection result for 5 minutes");

        return result;
    }
}
