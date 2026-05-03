package com.skytrust.service;

import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.service.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenBlacklistServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        SecurityUtil.init("test-jwt-key-for-blacklist-tests-only", 7200000, 604800000);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void addToBlacklist_shouldStoreTokenInRedis() {
        String token = SecurityUtil.generateToken("testuser");
        long expirationTime = System.currentTimeMillis() + 7200000;

        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(setOperations.add(anyString(), anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        assertDoesNotThrow(() -> tokenBlacklistService.addToBlacklist(token, expirationTime));
        verify(valueOperations).set(contains("jwt:blacklist:"), eq("1"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void isBlacklisted_shouldReturnTrueWhenFound() {
        when(redisTemplate.hasKey(contains("jwt:blacklist:"))).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted("some-token");

        assertTrue(result);
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenNotFound() {
        when(redisTemplate.hasKey(contains("jwt:blacklist:"))).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted("some-token");

        assertFalse(result);
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenTokenNull() {
        boolean result = tokenBlacklistService.isBlacklisted(null);
        assertFalse(result);
    }

    @Test
    void addToBlacklist_shouldHandleNullTokenGracefully() {
        assertDoesNotThrow(() -> tokenBlacklistService.addToBlacklist(null, System.currentTimeMillis()));
    }

    @Test
    void isBlacklisted_shouldReturnFalseOnRedisException() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

        boolean result = tokenBlacklistService.isBlacklisted("some-token");

        assertFalse(result);
    }
}
