package com.zaicev.task_tracker_backend.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.zaicev.task_tracker_backend.models.Token;

@ExtendWith(MockitoExtension.class)
public class JwtBlacklistServiceTest {
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
    private ValueOperations<String, String> valueOperations;
	
	@InjectMocks
    private JwtBlacklistService jwtBlacklistService;
	
	@Test
    void addTokenToBlacklist_shouldSetTokenInRedisWithTTL() {
        UUID tokenId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(3600);
        Token token = new Token(tokenId, "subject", Collections.emptyList(), createdAt, expiresAt);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        jwtBlacklistService.addTokenToBlacklist(token);

        long expectedTTL = Duration.between(createdAt, expiresAt).toMillis();
        verify(valueOperations).setIfAbsent(
                eq("token:" + tokenId.toString()),
                eq("blacklisted"),
                eq(expectedTTL),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void isBlacklisted_shouldReturnTrueIfTokenExistsInRedis() {
        UUID tokenId = UUID.randomUUID();
        Token token = new Token(tokenId, "subject", Collections.emptyList(), Instant.now(), Instant.now().plusSeconds(3600));

        when(redisTemplate.hasKey("token:" + tokenId.toString())).thenReturn(true);

        boolean result = jwtBlacklistService.isBlacklisted(token);

        assertTrue(result);
    }

    @Test
    void isBlacklisted_shouldReturnFalseIfTokenDoesNotExistInRedis() {
        UUID tokenId = UUID.randomUUID();
        Token token = new Token(tokenId, "subject", Collections.emptyList(), Instant.now(), Instant.now().plusSeconds(3600));

        when(redisTemplate.hasKey("token:" + tokenId.toString())).thenReturn(false);

        boolean result = jwtBlacklistService.isBlacklisted(token);

        assertFalse(result);
    }
}
