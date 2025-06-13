package com.zaicev.task_tracker_backend.services;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.models.Token;

@Service
public class JwtBlacklistService {
	private final RedisTemplate<String, String> redisTemplate;

	public JwtBlacklistService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	public void addTokenToBlacklist(Token token) {
		Duration ttl = Duration.between(token.createdAt(), token.expiresAt());
		redisTemplate.opsForValue().setIfAbsent("token:" + token.id().toString(), "blacklisted", ttl.toMillis(), TimeUnit.MILLISECONDS);
	}
	
	public boolean isBlacklisted(Token token) {
		return redisTemplate.hasKey("token:" + token.id().toString());
	}
}
