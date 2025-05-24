package org.example.prodcatservice.services;

import org.example.prodcatservice.clients.AuthClient;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCacheHitReturnsCachedDTO() {
        String tokenHeader = "Bearer abc.def.ghi";
        String token = "abc.def.ghi";
        String key = "token:introspect:" + token;

        TokenIntrospectionResponseDTO cachedDto = new TokenIntrospectionResponseDTO();
        cachedDto.setSub("user123");

        when(valueOperations.get(key)).thenReturn(cachedDto);

        TokenIntrospectionResponseDTO result = tokenService.introspect(tokenHeader);

        assertThat(result).isNotNull();
        assertThat(result.getSub()).isEqualTo("user123");

        verify(authClient, never()).validateToken(anyString()); // AuthClient not called
    }

    @Test
    void testCacheMissCallsAuthClientAndStoresResult() {
        String tokenHeader = "Bearer xyz.abc.123";
        String token = "xyz.abc.123";
        String key = "token:introspect:" + token;

        when(valueOperations.get(key)).thenReturn(null);

        TokenIntrospectionResponseDTO dto = new TokenIntrospectionResponseDTO();
        dto.setSub("user456");
        when(authClient.validateToken(tokenHeader)).thenReturn(dto);

        TokenIntrospectionResponseDTO result = tokenService.introspect(tokenHeader);

        assertThat(result).isNotNull();
        assertThat(result.getSub()).isEqualTo("user456");

        verify(authClient).validateToken(tokenHeader);
        verify(valueOperations).set(key, dto, 300, TimeUnit.SECONDS);
    }
}
