package com.zaicev.task_tracker_backend.security.cookie;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.zaicev.task_tracker_backend.security.models.Token;

import lombok.Setter;

public class DefaultTokenCookieFactory implements Function<Authentication, Token>{
	
	@Setter
	private Duration tokenTtl = Duration.ofMinutes(10);

	@Override
	public Token apply(Authentication authentication) {
		var now = Instant.now();
		return new Token(UUID.randomUUID(), authentication.getName(), authentication.getAuthorities()
										.stream()
										.map(GrantedAuthority::getAuthority).toList(), now, now.plus(tokenTtl));
	}	

}
